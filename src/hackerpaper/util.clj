(ns hackerpaper.util
  (:require [clojure.string :as str]))

(defn indent
  "Indent lines in `s` by `n` spaces."
  [s n]
  (when (some? s)
    (if (< n 1)
      s
      (let [space (str/join (repeat (* 4 n) " "))]
        (->> (str/split-lines s)
             (map #(str space %))
             (str/join "\n"))))))

(defn vectorify
  "Put `x` in a vector if it isn't one."
  [x]
  (if (vector? x) x [x]))
