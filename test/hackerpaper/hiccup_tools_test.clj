(ns hackerpaper.hiccup-tools-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
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
                 [:tag {:a "foo"}]))))

  (testing "Predicates from map specs with functions"
    (let [starts-with-x #(str/starts-with? % "x")]
      ;; Predicate matches attribute's value
      (is (true? ((#'h/selector-spec->pred {:a starts-with-x})
                  [:tag {:a "xa"}])))
      ;; Predicate matches multiple attributes' values
      (is (true? ((#'h/selector-spec->pred {:a starts-with-x
                                            :b starts-with-x})
                  [:tag {:a "xa", :b "xb"}])))
      ;; :a's value doesn't satisfy predicate
      (is (false? ((#'h/selector-spec->pred {:a starts-with-x
                                             :b starts-with-x})
                   [:tag {:a "ya", :b "xb"}])))
      ;; :b's value doesn't satisfy predicate
      (is (false? ((#'h/selector-spec->pred {:a starts-with-x
                                             :b starts-with-x})
                   [:tag {:a "xa", :b "yb"}])))
      ;; Both values don't satisfy predicate
      (is (false? ((#'h/selector-spec->pred {:a starts-with-x
                                             :b starts-with-x})
                   [:tag {:a "ya", :b "yb"}])))
      ;; Missing attributes
      (is (false? ((#'h/selector-spec->pred {:a starts-with-x
                                             :b starts-with-x})
                   [:tag {}]))))))
