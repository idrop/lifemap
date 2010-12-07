(ns lifemap.data  
  (:require
   [somnium.congomongo :as cm]))

(def events-collection :events)

;; init db
(cm/mongo! :db "lifemap")

;; index 
(cm/add-index! events-collection [:fbid])

(defn all
  "list of events matching fbid"
  [fbid]
  (cm/fetch events-collection :where {:fbid fbid} ))


(defn- insert [m]
  (cm/insert! events-collection (assoc m "created" (System/currentTimeMillis))))


(defn add
  "adds current map to events, and returns this entry as only entry in a list"
  [m]
  (->>
   (assoc m :created (System/currentTimeMillis))
   (cm/insert! events-collection)
   (list)))


(defn update
  "update a row, and return that row as list of one"
  [m]
  (let [
        tomerge (select-keys m [:tag :desc :when])
        id (cm/object-id (:id m))
        record (cm/fetch-by-id events-collection id)
        ]
    (cm/update! events-collection record (merge record tomerge))
    (list (cm/fetch-by-id events-collection id))))


(defn delete [id]
  (let [fbid (:fbid (cm/fetch-by-id events-collection (cm/object-id id)))]
    (cm/destroy! events-collection {:_id (cm/object-id id)})
    (all fbid)))

