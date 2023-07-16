;; Copyright © Manetu, Inc.  All rights reserved
;;
;; adapted from https://github.com/grimradical/clj-semver

(ns manetu.gitlab.semver
  (:require [clojure.string :as string]))

(def pattern #"^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$")

(defn valid-format?
  "Checks the string `s` for semantic versioning formatting"
  [s]
  (if (nil? (re-matches pattern s)) false true))

(defn- try-parse-int
  "Attempt to parse `o` to an int, returning `o` if that fails or the
  parsed version of `o` if successful."
  [o]
  (try
    (Integer/parseInt o)
    (catch NumberFormatException e o)))

(defn- valid?
  "Checks if the supplied version map is valid or not"
  [v]
  (and (map? v)
       (number? (:major v))
       (number? (:minor v))
       (number? (:patch v))
       (>= (:major v) 0)
       (>= (:minor v) 0)
       (>= (:patch v) 0)
       (or (nil? (:pre-release v))
           (string? (:pre-release v)))
       (or (nil? (:build v))
           (string? (:build v)))))

(defn- parse
  "Parses string `s` into a version map"
  [s]
  {:pre  [(string? s)
          (valid-format? s)]
   :post [(valid? %)]}
  (let [[[_ major minor patch pre-release build]] (re-seq pattern s)]
    {:major (try-parse-int major)
     :minor (try-parse-int minor)
     :patch (try-parse-int patch)
     :pre-release pre-release
     :build build}))

(defn- version
  "If `o` is a valid version map, does nothing. Otherwise, we'll
  attempt to parse `o` and return a version map."
  [o]
  {:post [(valid? %)]}
  (if (and (map? o) (valid? o))
    o
    (parse o)))

(defn- cmp
  "Compares versions a and b, returning -1 if a is older than b, 0 if
  they're the same version, and 1 if a is newer than b"
  [a b]
  {:post [(number? %)]}
  (let [try-parse-int #(try
                         (Integer/parseInt %)
                         (catch NumberFormatException e %))
        key-for-ident #(when %
                         (into [] (map try-parse-int (string/split % #"\."))))
        key           (juxt :major
                            :minor
                            :patch
                            ;; Because non-existent pre-release tags take
                            ;; precedence over existing ones
                            #(nil? (% :pre-release))
                            #(key-for-ident (:pre-release %))
                            #(key-for-ident (:build %)))]
    (compare (key (version a))
             (key (version b)))))

(defn newest
  "Returns the newest version within 'versions'"
  [versions]
  (first (sort #(cmp (version %2) (version %1)) versions)))
