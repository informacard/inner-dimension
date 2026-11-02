package com.innerdimension.domain;

/** A per-cast account: independent contacts share the highest pressure and entry fee. */
public final class MultiDomainBudget {
    private double paid;
    public record Debit(double remaining,double rate,double fee) {}
    public Debit debit(double remaining,double rate,double due,double alreadyRate,double alreadyPaid) {
        double fee=Math.max(0,due-Math.max(paid,alreadyPaid));
        paid=Math.max(paid,Math.max(due,alreadyPaid));
        double extra=Math.max(0,Math.max(1,rate)-Math.max(0,alreadyRate));
        return new Debit(Math.max(0,remaining-extra-fee),extra,fee);
    }
    public double paid(){return paid;}
}
