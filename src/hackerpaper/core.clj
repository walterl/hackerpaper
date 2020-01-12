(ns hackerpaper.core
  (:require [hackerpaper
             [hiccup-tools :as h]
             [hn-thread :as hn]
             [util :as u]]
            [hickory.core :as hick])
  (:gen-class))

(defn parse
  "Parse HN thread from `f` (HTML file or URL)."
  [f]
  (let [hiccup          (-> (slurp f) hick/parse hick/as-hiccup)
        hnmain          (h/select (first hiccup) [:table {:id "hnmain"}])
        fatitem-rows    (-> hnmain
                            (h/select [:table {:class "fatitem"}])
                            (h/select [:tbody]))
        comments-trs    (-> hnmain
                            (h/select [:table {:class "comment-tree"}])
                            (h/select-all [:tr {:class "athing comtr "}]))]
    {:title    (-> hnmain
                   (h/select [:tr {:id "pagespace"}])
                   h/attrs
                   :title)
     :author   (hn/user-name fatitem-rows)
     :question (-> (h/children fatitem-rows)
                   (nth 4)
                   last
                   h/children
                   hn/block-text)
     :comments (hn/comments->comment-tree (map hn/comment-tr->comment comments-trs))}))

(defn ->yaml
  "Convert parsed HN thread to a YAML string."
  [{:keys [title author question comments]}]
  (str
   (str "title: " title "\n")
   (str "author: " author "\n")
   (str "question: >\n" (u/indent question 1) "\n")
   (str "comments:\n")
   (hn/comments-tree->yaml comments)))

(defn -main
  "I don't do a whole lot ... yet."
  [f & _args]
  (println (-> f parse ->yaml)))
