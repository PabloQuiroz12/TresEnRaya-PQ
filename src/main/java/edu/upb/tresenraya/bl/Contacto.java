package edu.upb.tresenraya.bl;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contacto implements Serializable {

    private String name;
    private String ip;
    private boolean stateConnect = false;
    
    public Contacto(String ip, boolean stateConnect) {
        this.ip = ip;
        this.stateConnect = stateConnect;
    }
}
