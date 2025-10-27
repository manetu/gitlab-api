;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.gitlab.download.generic-package
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as s]
            [babashka.curl :as curl]
            [taoensso.timbre :as log]
            [manetu.gitlab.api :as gitlab]
            [manetu.gitlab.semver :as semver]))

(def not-blank? (complement string/blank?))

(s/def ::version semver/valid-format?)
(s/def ::package-name not-blank?)
(s/def ::project-id not-blank?)
(s/def ::options (s/keys :req-un [::version ::package-name ::project-id]))

(defn valid-options?
  [data]
  (or (s/valid? ::options data)
      (throw (ex-info "invalid argument" (s/explain-data ::options data)))))

(defn- compute-api-prefix
  [{:keys [project-id]}]
  (str "/projects/" project-id "/packages"))

(defn- get-packages
  [{:keys [package-name] :as options}]
  (gitlab/invoke-allpages {} "GET" (str (compute-api-prefix options) "?package_name=" package-name)))

(defn- has-prefix?
  [prefix s]
  (log/debug "prefix:" prefix "s:" s)
  (when s
    (some? (re-find (re-pattern (str "^" prefix)) s))))

(defn- open-download
  [{:keys [package-name package-file] :as options} version]
  (let [package-file (or package-file (str package-name ".tgz"))
        url (str gitlab/gitlab-api-base (compute-api-prefix options) "/generic/" package-name "/" version "/" package-file)]
    (log/debug "url:" url)
    (:body
      (curl/get url {:headers {"PRIVATE-TOKEN" gitlab/gitlab-token}
                     :as      :bytes}))))

(defn download!
  [{:keys [version] :as options}]
  {:pre [(valid-options? options)]}
  (let [packages        (get-packages options)
        newest-version  (->> packages (map :version) (filter (partial has-prefix? version)) semver/newest)]
    (log/info "downloading version:" newest-version)
    (open-download options newest-version)))