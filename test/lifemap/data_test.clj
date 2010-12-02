(ns lifemap.data-test
  (:use
   clojure.test)
  (:require
   [lifemap.data :as data]
   [somnium.congomongo :as cm]))

;; for each test, rebind the mongo collection name to :events-test
;; see def m-coll in data.clj
(defn for-each-test [f]
  (binding [data/m-coll :eventstest]
    (f)))

(use-fixtures :each for-each-test)

(deftest should-return-single-event-in-list-when-adding-to-event []
         (let [
               id (System/currentTimeMillis)
               list-after-adding (data/add {:id id})
               ]
           (prn (str "added.." list-after-adding))
           (is (= 1 (count list-after-adding)) "should only be one event in list")
           (is (= id (:id (first list-after-adding))) "should be same id that we added")))
