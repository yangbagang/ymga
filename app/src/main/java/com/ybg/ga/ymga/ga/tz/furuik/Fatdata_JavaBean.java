package com.ybg.ga.ymga.ga.tz.furuik;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangbagang on 15/10/9.
 */
public class Fatdata_JavaBean {

    String action;
    String error;
    List<Fat> params;

    public String getAction() {
        return action;
    }

    public String getError() {
        return error;
    }

    public List<Fat> getParams() {
        return params;
    }

    public class Fat implements Serializable {

        String age;
        String birth;
        String bmi;
        String bone;
        String datetime;
        String email;
        String fat;
        String gender;
        String heat;
        String height;
        String id;
        String level;
        String muscle;
        String name;
        String viscus;
        String water;
        String weight;


        public void setAge(String age) {
            this.age = age;
        }

        public void setBirth(String birth) {
            this.birth = birth;
        }

        public String getAge() {
            return age;
        }

        public String getBirth() {
            return birth;
        }

        public String getBmi() {
            return bmi;
        }

        public String getBone() {
            return bone;
        }

        public String getDatetime() {
            return datetime;
        }

        public String getEmail() {
            return email;
        }

        public String getFat() {
            return fat;
        }

        public String getGender() {
            return gender;
        }

        public String getHeat() {
            return heat;
        }

        public String getHeight() {
            return height;
        }

        public String getId() {
            return id;
        }

        public String getLevel() {
            return level;
        }

        public String getMuscle() {
            return muscle;
        }

        public String getName() {
            return name;
        }

        public String getViscus() {
            return viscus;
        }

        public String getWater() {
            return water;
        }

        public String getWeight() {
            return weight;
        }

    }

}
