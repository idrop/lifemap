(ns lifemap.data  
  (:require
   [somnium.congomongo :as cm]))

(def m-coll :events)

;; init db
(cm/mongo! :db "lifemap")

;; index 
(cm/add-index! m-coll [:fbid])

(defn all
  "list of events matching fbid"
  [fbid]
  (cm/fetch m-coll :where {:fbid fbid} ))


(defn- insert [m]
  (cm/insert! m-coll (assoc m "created" (System/currentTimeMillis))))


(defn add
  "adds current map to events, and returns this entry as only entry in a list"
  [m]
  (->>
   (assoc m :created (System/currentTimeMillis))
   (cm/insert! m-coll)
   (list)))


(defn update
  "update a row, and return that row as list of one"
  [m]
  (let [
        tomerge (select-keys m [:tag :desc :when])
        id (cm/object-id (:id m))
        record (cm/fetch-by-id m-coll id)
        ]
    (prn (str "to merge " tomerge))
    (prn (str "into record " record))
    (cm/update! m-coll record (merge record tomerge))
    (list (cm/fetch-by-id m-coll id))))


(defn delete [id]
  (let [fbid (:fbid (cm/fetch-by-id m-coll (cm/object-id id)))]
    (cm/destroy! m-coll {:_id (cm/object-id id)})
    (all fbid)))

