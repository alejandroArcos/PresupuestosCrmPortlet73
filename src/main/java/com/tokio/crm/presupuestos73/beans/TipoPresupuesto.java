package com.tokio.crm.presupuestos73.beans;

public enum TipoPresupuesto {

    PRODUCCION(0),GASTO_BUDGET(1),GASTO_REAL(2) ;

    private int valor;

    private TipoPresupuesto(int valor) {
        this.valor = valor;
    }

    public int getTipoPresupuesto() {
        return valor;
    }
}
