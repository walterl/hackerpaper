(ns hackerpaper.hiccup-tools)

(defn tag
  "Returns tag of Hiccup-style node."
  [node]
  (first node))

(defn attrs
  "Returns attributes of Hiccup-style node."
  [node]
  (second node))

(defn children
  "Returns seq of children from Hiccup-style node."
  [node]
  (drop 2 node))

(defn walk
  "Walk a Hiccup structure"
  [node]
  (cons node
        (mapcat identity
                (for [child (children node)
                      :when (not (string? child))]
                  (walk child)))))

(defn- attrs-item->pred
  [[attr value]]
  (fn [node]
    (let [node-attrs (attrs node)]
      (and (contains? node-attrs attr)
           (= value (node-attrs attr))))))

(defn- selector-spec->pred
  "Maps the given selector spec to a node predicate."
  [spec]
  (cond
    (keyword? spec)
    (fn [node] (= (tag node) spec))

    (map? spec)
    (apply every-pred (map attrs-item->pred spec))

    (fn? spec)
    spec))

(defn selector
  "Returns a predicate that returns true for Hiccup nodes matching the given
  spec.

  Specs are vectors of keywords (matched to tag name), maps (matched to
  attributes) or node predicates.

  All spec predicates must be satisfied for the constructed selector to return
  true."
  [[& specs]]
  (apply every-pred (mapv selector-spec->pred specs)))

(defn select-all
  "Convenience function for the following:

      (filter (selector selector-specs) (walk node))."
  [node selector-specs]
  (filter (selector selector-specs) (walk node)))

(defn select
  "Convenience function for the following:

      (first (select-all node selector-specs))."
  [node selector-specs]
  (first (select-all node selector-specs)))
