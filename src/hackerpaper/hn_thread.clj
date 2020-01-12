(ns hackerpaper.hn-thread
  (:require [clojure.string :as str]
            [hackerpaper
             [hiccup-tools :as h]
             [util :as u]]))

(defn user-name
  "Returns the user name from the first `hnuser` link found in `node`'s
  sub-tree."
  [node]
  (-> (h/walk node)
      (h/select [:a {:class "hnuser"}])
      h/children
      last))

(defn comment-age
  "Extracts the comment age from the given `comm`ent `<tr>`-node."
  [comm]
  (-> (h/walk comm)
      (h/select [:span {:class "age"}])
      (h/select [:a])
      h/children
      last))

(defn paragraph->str
  "Converts a `:p`aragraph node to a string."
  [p]
  (if (and (vector? p)
           (= :p (h/tag p)))
    (str/join (h/children p)) ; There _should_ be only one child
    p))

(defn drop-reply-link
  "Drops the HN comment's \"reply\" link from the collection of comment text
  children.

  Sometimes the \"reply\" link is outside of the comment text node, so we have
  to check if it's there."
  [commtext-children]
  (if ((h/selector [:div {:class "reply"}]) (last commtext-children))
    (butlast commtext-children)
    commtext-children))

(defn block-text
  "Extract text string from block of structured text."
  [xs]
  (->> xs
       (map paragraph->str)
       ;; TODO Convert :i elements to Markdown _italics_
       ;; TODO Convert :a elements to Markdown [links](...)
       (map #(str/replace % "\n" " ")) ; Remove extra newlines, parsed from HTML line breaks
       (map #(str/replace % "  " " "))
       (map str/trim)
       (str/join "\n\n")))

(defn comment-content
  "Returns the comment content, converted to text."
  [comm]
  (-> (h/walk comm)
      (h/select [{:class #(str/starts-with? % "commtext ")}])
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

(defn- append-comment
  ([tree comm]
   (append-comment tree comm (:level comm)))
  ([tree comm level]
   (if (zero? level)
     (conj tree [comm])
     (conj (pop tree) (append-comment (u/vectorify (last tree)) comm (dec level))))))

(defn comments->comment-tree
  "Group `comments` into a tree.

  The returned tree is similar to a Hiccup element tree: elements are vectors
  of comments (first element) and replies (all other elements)."
  [comments]
  (reduce append-comment [] comments))

(defn comment-tr->comment
  "Convert comment HTML (`<tr>`) node to a comment data structure."
  [comment-tr]
  {:age     (comment-age comment-tr)
   :author  (user-name comment-tr)
   :comment (comment-content comment-tr)
   :level   (comment-level comment-tr)})

(defn comment->yaml
  "Converts the specified `comm`ent and its `replies` to YAML, indented
  `indent` levels."
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
  "Convert `comments` tree to YAML-like lines.
  
  This is just the entry point. `comment->yaml` does the heavy lifting of
  actually rendering the comments and their replies."
  ([comments]
   (comments-tree->yaml comments 0))
  ([comments indent]
   (str/join "\n\n" (mapv #(comment->yaml % indent) comments))))
