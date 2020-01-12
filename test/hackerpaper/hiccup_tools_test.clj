(ns hackerpaper.hiccup-tools-test
  (:require [clojure.test :refer :all]
            [hackerpaper.hiccup-tools :as h]))

(deftest selector-spec-pred-test
  (testing "Predicates from map specs with values"
    ;; Single attribute and value matches
    (is (true? ((#'h/selector-spec->pred {:a "foo"})
                [:tag {:a "foo"}])))
    ;; Single attribute and value matches among multiple values
    (is (true? ((#'h/selector-spec->pred {:a "foo"})
                [:tag {:a "foo", :b "bar", :c "baz"}])))
    ;; Multiple attributes and their values match
    (is (true? ((#'h/selector-spec->pred {:a "foo", :c "baz"})
                [:tag {:a "foo", :b "bar", :c "baz"}])))
    ;; Fails when attribute is present, but value doesn't match
    (is (false? ((#'h/selector-spec->pred {:a "bar"})
                 [:tag {:a "foo"}])))
    ;; Fails when value is present, but attribute doesn't match
    (is (false? ((#'h/selector-spec->pred {:b "foo"})
                 [:tag {:a "foo"}])))))
