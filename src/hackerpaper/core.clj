(ns hackerpaper.core
  (:require [clojure.string :as str]
            [hackerpaper
             [hiccup-tools :as h]
             [hnthread :as hn]
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
                            (h/select-all [:tr {:class "athing comtr "}]))

        title           (-> hnmain
                            (h/select [:tr {:id "pagespace"}])
                            h/attrs
                            :title)
        question        (-> (h/children fatitem-rows)
                            (nth 4)
                            last
                            h/children
                            hn/block-text)
        question-author (hn/search-hnuser fatitem-rows)
        comments        (->> comments-trs
                             (map hn/->comment)
                             (hn/comments->comment-tree))]
    {:title    title
     :author   question-author
     :question question
     :comments comments}))

(defn to-yaml
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
  (println (-> f parse to-yaml)))
