package com.ruoyi;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @ClassName TPass
 * @Description TODO
 * @Author px
 * @Version 1.0
 */
public class TPass {
    public static void main(String[] args) {

        //admin $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String temp=encoder.encode("dhnd@123!");
        System.out.println(temp);

    }
}
