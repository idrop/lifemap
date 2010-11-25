(ns lifemap.server-test
  (use :reload-all
       clojure.test
       [lifemap.server :as server]))


(def valid-data (translate (keywordize data)))

(def data { "lng" "-0.13677564177418056" "lat" "51.311495428201916" "fbid" "642791293"
                        "when" "62985600000" "desc" "descrition" "tag" "smaoke2"} )

(deftest should-not-return-nil []
         (is (not (= nil (server/valid {})))))

(deftest is-valid-request []
         (is (server/valid {:fbid 1 :tag "tag" :when 1 :lat 90.0 :lng -180.0})))

(deftest fbid-must-be-number []
         (is (not (server/valid nil)))
         (is (not (server/valid -1))) 
         (is (not (server/valid "1")))

         )

(deftest tag-must-not-be-nil []
         (is (not (server/valid {:fbid 1 :tag nil}))))

(deftest tag-must-be-non-blank-text []
         (is (not (server/valid {:fbid 1 :tag ""})))
         (is (not (server/valid {:fbid 1 :tag nil})))
         (is (not (server/valid {:fbid 1 :tag "\n \n \t"}))))


(deftest date-must-not-be-nil []
         (is (not (server/valid {:fbid 1 :tag "tag" :when nil}))))

(deftest date-must-be-less-than-now []
         (is (not (server/valid {:fbid 1 :tag "tag" :when (+ 10000 (.getTime (java.util.Date.)))}))))

(deftest lat-must-not-be-nil []
         (is (not (server/valid {:fbid 1 :tag "tag" :when 0 :lat nil}))))

(deftest lat-must-be-between-minus90-and-90-degrees []
         (is (not (server/valid {:fbid 1 :tag "tag" :when 0 :lat 90.01})))
         (is (not (server/valid {:fbid 1 :tag "tag" :when 0 :lat -90.01}))))


(deftest lng-must-be-between-minus-180-and-180-degrees []
         (is (not (server/valid {:fbid 1 :tag "tag" :when 0 :lat 90.0 :lng 180.01})))
         (is (not (server/valid {:fbid 1 :tag "tag" :when 0 :lat -90.0 :lng -180.01}))))


(deftest is-valid-real-request []
         (is (server/valid valid-data )))

(deftest is-valid-2 []
         (is (server/valid {:fbid 1 :tag "tag" :desc "desc" :when 1 :lat 1.0 :lng 1.0} )))

(deftest tag-is-valid-when-length-30 []
         (is (server/valid (assoc valid-data :tag (apply str (take 30 (repeat \a)))))))

(deftest tag-is-invalid-when-length-31 []
         (is (not (server/valid (assoc valid-data :tag (apply str (take 31 (repeat \a)))))) ))

(deftest ok-for-desc-to-be-nil []
         (is (server/valid (assoc valid-data :desc nil))))

(deftest desc-may-be-200-chars []
         (is (server/valid (assoc valid-data :desc (apply str (take 200 (repeat \a)))))))

(deftest desc-may-not-be-greater-than-200-chars []
         (is (not (server/valid (assoc valid-data :desc (apply str (take 201 (repeat \a)))))) ))
