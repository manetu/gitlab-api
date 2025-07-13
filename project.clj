(defproject io.github.manetu/gitlab-api "1.1.2-SNAPSHOT"
  :description "A babashka compatible library for accessing the gitlab API "
  :url "https://github.com/manetu/gitlab-api"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [babashka/babashka.curl "0.1.2"]
                 [environ "1.2.0"]
                 [medley "1.4.0"]
                 [cheshire "5.11.0"]
                 [com.taoensso/timbre "5.2.1"]]
  :repl-options {:init-ns user}
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.4.4"]
                                  [criterium "0.4.6"]
                                  [eftest "0.6.0"]]}})
