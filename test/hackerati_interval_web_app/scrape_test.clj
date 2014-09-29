(ns hackerati-interval-web-app.scrape-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io] 
            [hackerati-interval-web-app.scrape :refer :all]))

(deftest get-price-from-file-test
  (testing "Validating that function returns actual product price from local HTML."
    (is (= (get-price-from-file 
            (io/resource "test/amazon-prescript-assist")) 
           "$81.99"))
    (is (= (get-price-from-file 
            (io/resource "test/amazon-cryptonomicon"))
           "$5.44"))
    (is (= (get-price-from-file 
            (io/resource "test/amazon-roomba"))
           "$699.99"))
    (is (= (get-price-from-file 
            (io/resource "test/amazon-chukkas"))
           "$68.87"))))

(deftest get-price-from-url-test
  (testing "Validating that function returns actual product price from local HTML."
    (is (= (get-price-from-url 
            "http://www.amazon.com/gp/aw/d/0060512806/ref=mp_s_a_1_1?qid=1411578682&sr=8-1") 
           "$5.44"))
    (is (= (get-price-from-url 
            "http://www.amazon.com/gp/aw/d/0544272994/ref=mr_books_bs_p1_")
           "$14.40"))
    (is (= (get-price-from-url 
             "http://www.amazon.com/gp/aw/d/B00IO9PBPS/ref=mp_s_a_1_1?qid=1411579413&sr=8-1")
           "$671.89"))
    (is (= (get-price-from-url 
            "http://www.amazon.com/gp/aw/d/B0059715SA/ref=aw_d_var_2nd_shoes_img?ca=B005970V3U&vs=1")
           "$68.87"))))

(deftest mobile-url?-test
  (testing "Testing function's ability to discern mobile Amazon URLs."
    (is (= (mobile-url? "http://www.amazon.com/gp/aw/d/0060512806/ref=mp_s_a_1_1?qid=1411578682&sr=8-1") true))
    (is (= (mobile-url? "http://www.amazon.com/iRobot-Roomba-Vacuum-Cleaning-Allergies/dp/B00IO9PBPS") false))
))

(deftest get-mobile-url-test
  (is (= (get-mobile-url "http://www.amazon.com/iRobot-Roomba-Vacuum-Cleaning-Allergies/dp/B00IO9PBPS")
         "http://www.amazon.com/gp/aw/d/B00IO9PBPS"))
  (is (= (get-mobile-url "http://www.amazon.com/gp/product/0615314465/ref=s9_simh_gw_p14_d0_i2?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=center-6&pf_rd_r=05WVS8TH2RNG64Q0JGM8&pf_rd_t=101&pf_rd_p=1688200302&pf_rd_i=507846")
         "http://www.amazon.com/gp/aw/d/0615314465")))
