;; Copyright © Manetu, Inc.  All rights reserved

(ns manetu.gitlab.api
  (:require [clojure.string :as string]
            [babashka.curl :as curl]
            [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [medley.core :as m]
            [cheshire.core :as json]))

(def default-api-base "https://gitlab.com/api/v4")

;; env-vars
(def gitlab-token    (env :gitlab-token))
(def gitlab-api-base (env :gitlab-api-base default-api-base))

(def verbs
  {"GET"  curl/get
   "POST" curl/post
   "PUT"  curl/put})

(defn- generate-headers []
  {:headers (cond-> {"Accept" "application/json"}
              (not (string/blank? gitlab-token)) (assoc "PRIVATE-TOKEN" gitlab-token))})

(defn- invoke!
  "Invoke the API endpoint, returning status/header/body as a native map"
  [options verb path]
  (let [params (generate-headers)
        url    (str gitlab-api-base path)]
    (log/debug "invoking curl at" url "with" params) 
    (let [{:keys [status] :as r} ((get verbs verb) url params)]
      (log/debug "result:" status)
      (-> r
          (select-keys [:status :headers :body])
          (update :body #(json/parse-string % true))))))

(defn invoke-raw
  "invokes the api endpoint without any pagination handling"
  [options verb path]
  (:body (invoke! options verb path)))

(defn invoke-allpages
  "Like (invoke-raw), but will load all available pages"
  [options verb path]
  (loop [acc nil page 1]
    (let [{:keys [status body headers]} (invoke! options verb (str path "&page=" page))
          {:keys [x-page x-total-pages]} (as-> headers $
                                             (m/map-keys keyword $)
                                             (select-keys $ [:x-page :x-total-pages])
                                             (m/map-vals #(Integer/parseInt %) $))]
     (when (and (>= status 200) (< status 400))
       (let [acc (concat acc body)]
         (if (>= x-page x-total-pages)
           acc
           (recur acc (inc x-page))))))))
