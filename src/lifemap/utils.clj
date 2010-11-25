(ns lifemap.utils
  (:use
   [clojure.contrib.json :as json]) )


(defn valid?
  "true if map is valid"
  [ { :keys [fbid tag desc when lat lng] } ]

  (and
   (number? fbid)
   (< 0 fbid)
   (string? tag) 
   (not (every? #(Character/isWhitespace %) tag))
   (>= 40 (count tag))
   (if (nil? desc)
     true
     (>= 200 (count desc)))
   (number? when)
   (> (.getTime (java.util.Date.)) when)
   (float? lat)
   (<= -90.0 lat)
   (>= 90.0 lat)
   (float? lng)
   (<= -180 lng)
   (>= 180 lng)
   ))



(defn string-to-long [s] (Long/parseLong s))

(defn string-to-float [s] (Float/parseFloat s))

(defn translate [m]
  (assoc m
    :fbid (string-to-long (:fbid m))
    :when (string-to-long (:when m))
    :lat (string-to-float (:lat m))
    :lng (string-to-float (:lng m))))

(defn keywordize [m]
  (into
   (empty m)
   (for [[k v] m]
     [(keyword k) v])))

(defn to-json [m]
  {
   :status 200
   :headers {"Content-Type" "application/json"}
   :body  (json/json-str m)})


(defn clean-response [coll]
  (->>
   (map #(assoc % :id (.toString (:_id %))) coll)
   (map #(dissoc % :_id :created ,,,))))

