 (ns lifemap.data  
   (:use
    [somnium.congomongo :as cm] ))

(cm/mongo! :db "lifemap")

(cm/add-index! :events [:fbid])


(defn all
  "list of json strs matching fbid"
  [fbid]
  (cm/fetch :events :where {:fbid fbid} ))


(defn- insert [m]
  (cm/insert! :events (assoc m "created" (System/currentTimeMillis))))


(defn add
  "adds current map to events, and returns this entry as only entry in a list"
  [m]
  (->>
   (assoc m :created (System/currentTimeMillis))
   (cm/insert! :events)
   (list)))


(defn update
  "update a row, and return that row as list of one"
  [m]
  (let [
        tomerge (select-keys m [:tag :desc :when])
        id (object-id (:id m))
        record (fetch-by-id :events id)
        ]
    (prn (str "to merge " tomerge))
    (prn (str "into record " record))
    (cm/update! :events record (merge record tomerge))
    (list (fetch-by-id :events id))))


(defn delete [id]
  (let [fbid (:fbid (fetch-by-id :events (object-id id)))]
    (cm/destroy! :events {:_id (object-id id)})
    (all fbid)))

