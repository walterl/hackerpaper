(ns hackerpaper.hnthread
  (:require [clojure.string :as str]
            [hackerpaper
             [hiccup-tools :as h]
             [util :as u]]))

(defn search-hnuser
  [comm]
  (-> (h/walk comm)
      (h/select [:a {:class "hnuser"}])
      h/children
      last))

(defn comment-age
  [comm]
  (-> (h/walk comm)
      (h/select [:span {:class "age"}])
      (h/select [:a])
      h/children
      last))

(defn paragraph->str
  [p]
  (if (and (vector? p)
           (= :p (h/tag p)))
    (str/join (h/children p)) ; There _should_ be only one child
    p))

(defn drop-reply-link
  [commtext-children]
  (if ((h/selector [:div {:class "reply"}]) (last commtext-children))
    (butlast commtext-children)
    commtext-children))

(defn block-text
  "Extract text string from block of structured text."
  [xs]
  (->> xs
       (map paragraph->str)
       (map #(str/replace % "\n" " ")) ; Remove extra newlines, parsed from HTML line breaks
       (map #(str/replace % "  " " "))
       (map str/trim)
       (str/join "\n\n")))

(defn comment-content
  [comm]
  (-> (h/walk comm)
      (h/select [#(str/starts-with? (or (-> % h/attrs :class) "") "commtext ")])
      h/children
      drop-reply-link
      block-text))

(defn comment-level
  "Determines the `comm`ent's tree level, based on its indentation."
  [comm]
  (when-let [indent (-> (h/walk comm)
                        (h/select [:td {:class "ind"}])
                        h/children
                        first
                        h/attrs
                        :width)]
    (/ (Integer/parseUnsignedInt indent) 40)))

(defn append-comment
  ([tree comm]
   (append-comment tree comm (:level comm)))
  ([tree comm level]
   (if (zero? level)
     (conj tree [comm])
     (conj (pop tree) (append-comment (u/vectorify (last tree)) comm (dec level))))))

(defn comments->comment-tree
  "Group `comments` into tree"
  [comments]
  (reduce append-comment [] comments))

(defn ->comment
  [comment-tr]
  {:age     (comment-age comment-tr)
   :author  (search-hnuser comment-tr)
   :comment (comment-content comment-tr)
   :level   (comment-level comment-tr)
   #_#_:tr      comment-tr})

(defn comment->yaml
  [[comm & replies] indent]
  (u/indent
   (str
    "- author: " (:author comm) "\n"
    "  age:    " (:age comm) "\n"
    "  comment: >\n"
    (u/indent (:comment comm) 1)
    (when-not (empty? replies)
      (str "\n  replies:\n"
           (str/join "\n\n" (mapv #(comment->yaml % 1) replies)))))
   indent))

(defn comments-tree->yaml
  "Convert `comments` to YAML-like lines"
  ([comments]
   (comments-tree->yaml comments 0))
  ([comments indent]
   (str/join "\n\n" (mapv #(comment->yaml % indent) comments))))
