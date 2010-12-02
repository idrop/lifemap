(ns lifemap.server-test
  (use clojure.test
       [lifemap.server :as server]
       [lifemap.utils :as utils]))

(def data { "lng" "-0.13677564177418056" "lat" "51.311495428201916" "fbid" "642791293"
                        "when" "62985600000" "desc" "descrition" "tag" "smaoke2"} )
(def valid-data (utils/translate (keywordize data)))

(deftest should-not-return-nil []
         (is (not (= nil (utils/valid? {})))))

(deftest is-valid-request []
         (is (utils/valid? {:fbid 1 :tag "tag" :when 1 :lat 90.0 :lng -180.0})))

(deftest fbid-must-be-number []
         (is (not (utils/valid? nil)))
         (is (not (utils/valid? -1))) 
         (is (not (utils/valid? "1")))

         )

(deftest tag-must-not-be-nil []
         (is (not (utils/valid? {:fbid 1 :tag nil}))))

(deftest tag-must-be-non-blank-text []
         (is (not (utils/valid? {:fbid 1 :tag ""})))
         (is (not (utils/valid? {:fbid 1 :tag nil})))
         (is (not (utils/valid? {:fbid 1 :tag "\n \n \t"}))))


(deftest date-must-not-be-nil []
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when nil}))))

(deftest date-must-be-less-than-now []
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when (+ 10000 (.getTime (java.util.Date.)))}))))

(deftest lat-must-not-be-nil []
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when 0 :lat nil}))))

(deftest lat-must-be-between-minus90-and-90-degrees []
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when 0 :lat 90.01})))
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when 0 :lat -90.01}))))


(deftest lng-must-be-between-minus-180-and-180-degrees []
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when 0 :lat 90.0 :lng 180.01})))
         (is (not (utils/valid? {:fbid 1 :tag "tag" :when 0 :lat -90.0 :lng -180.01}))))


(deftest is-valid-real-request []
         (is (utils/valid? valid-data )))

(deftest is-valid-2 []
         (is (utils/valid? {:fbid 1 :tag "tag" :desc "desc" :when 1 :lat 1.0 :lng 1.0} )))

(deftest tag-is-valid-when-length-40 []
         (is (utils/valid? (assoc valid-data :tag (apply str (take 40 (repeat \a)))))))

(deftest tag-is-invalid-when-length-41 []
         (is (not (utils/valid? (assoc valid-data :tag (apply str (take 41 (repeat \a)))))) ))

(deftest ok-for-desc-to-be-nil []
         (is (utils/valid? (assoc valid-data :desc nil))))

(deftest desc-may-be-200-chars []
         (is (utils/valid? (assoc valid-data :desc (apply str (take 200 (repeat \a)))))))

(deftest desc-may-not-be-greater-than-200-chars []
         (is (not (utils/valid? (assoc valid-data :desc (apply str (take 201 (repeat \a)))))) ))
