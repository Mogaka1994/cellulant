package com.moha.backend.chama.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author moha
 */
@Entity
@Table(name = "CHAMA")
public class Chama implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "CHAMA_NAME")
    private String chama_name;
    @Column(name = "CHAMA_ID")
    private String chama_id;
    
     public Chama(){
    
     }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getChama_name() {
        return chama_name;
    }

    public void setChama_name(String chama_name) {
        this.chama_name = chama_name;
    }

    public String getChama_id() {
        return chama_id;
    }

    public void setChama_id(String chama_id) {
        this.chama_id = chama_id;
    }

 
     
}

    
    

