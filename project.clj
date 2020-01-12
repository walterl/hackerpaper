(defproject hackerpaper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPLv3"
            :url  "https://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hickory "0.7.1" :exclusions [org.clojure/clojure]]]
  :main ^:skip-aot hackerpaper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
