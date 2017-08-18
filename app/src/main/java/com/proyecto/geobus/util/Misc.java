package com.proyecto.geobus.util;

public class Misc {

    public static String leyendaMinutosSegundos(float seg) {
        int minutos = 0;
        int segundos = 0;
        if(seg >= 60){
            minutos = (int) (seg/60);
            segundos = (int) (seg- minutos*60);
        } else {
            segundos = (int) seg;
        }
        return minutos+" min. "+segundos+" seg.";
    }

    public static int convertirAKMPorSegundo(double metrosXSegundo) {
        return (int) ((metrosXSegundo*3600)/1000);
    }

}
