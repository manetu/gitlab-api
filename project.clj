(defproject io.github.manetu/gitlab-api "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [babashka/babashka.curl "0.1.2"]
                 [environ "1.2.0"]
                 [medley "1.4.0"]
                 [cheshire "5.11.0"]
                 [com.taoensso/timbre "5.2.1"]])
