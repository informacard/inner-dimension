package com.innerdimension.projection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Small bounded arithmetic language. Expressions never execute Java or scripts. */
public final class PolarExpression {
    @FunctionalInterface public interface Node { double value(double r, double theta); }
    private final String text;
    private int position, nodes, depth;
    private PolarExpression(String text) { this.text = text.toLowerCase(Locale.ROOT); }
    public static Node compile(String text) {
        if (text == null || text.isBlank() || text.length() > 512)
            throw new IllegalArgumentException("Expression must contain 1..512 characters");
        PolarExpression parser = new PolarExpression(text);
        Node result = parser.or();
        parser.space();
        if (parser.position != parser.text.length()) throw parser.error("Unexpected input");
        return result;
    }
    public static boolean draws(Node node, double r, double theta) {
        double value = node.value(r, theta);
        return Double.isFinite(value) && value > 0;
    }
    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " @ " + (position + 1));
    }
    private void space() { while (position < text.length() && Character.isWhitespace(text.charAt(position))) position++; }
    private boolean take(String token) {
        space();
        if (!text.startsWith(token, position)) return false;
        position += token.length(); return true;
    }
    private Node bounded(Node node) { if (++nodes > 192) throw error("Expression too complex"); return node; }
    private Node or() {
        Node n = and();
        while (take("||")) { Node a=n,b=and(); n=bounded((r,t)->a.value(r,t)>0 || b.value(r,t)>0 ? 1:0); }
        return n;
    }
    private Node and() {
        Node n = comparison();
        while (take("&&")) { Node a=n,b=comparison(); n=bounded((r,t)->a.value(r,t)>0 && b.value(r,t)>0 ? 1:0); }
        return n;
    }
    private Node comparison() {
        Node n = sum();
        String op = null;
        for (String candidate : new String[]{">=","<=","==","!=",">","<"}) if (take(candidate)) { op=candidate; break; }
        if (op == null) return n;
        Node a=n,b=sum(); String operator=op;
        return bounded((r,t)->{
            double x=a.value(r,t),y=b.value(r,t);
            if (!Double.isFinite(x)||!Double.isFinite(y)) return 0;
            return switch(operator) { case ">="->x>=y?1:0; case "<="->x<=y?1:0; case "=="->x==y?1:0;
                case "!="->x!=y?1:0; case ">"->x>y?1:0; default->x<y?1:0; };
        });
    }
    private Node sum() {
        Node n=product();
        while (true) { Node a=n;
            if (take("+")) { Node b=product(); n=bounded((r,t)->a.value(r,t)+b.value(r,t)); }
            else if(take("-")) { Node b=product(); n=bounded((r,t)->a.value(r,t)-b.value(r,t)); }
            else return n;
        }
    }
    private Node product() {
        Node n=unary();
        while(true) { Node a=n;
            if(take("*")) { Node b=unary(); n=bounded((r,t)->a.value(r,t)*b.value(r,t)); }
            else if(take("/")) { Node b=unary(); n=bounded((r,t)->a.value(r,t)/b.value(r,t)); }
            else if(take("%")) { Node b=unary(); n=bounded((r,t)->{double x=a.value(r,t),y=b.value(r,t);return x-Math.floor(x/y)*y;}); }
            else return n;
        }
    }
    private Node unary() {
        if(++depth>32) throw error("Expression nesting too deep");
        try {
            if(take("+")) return unary();
            if(take("-")) { Node n=unary(); return bounded((r,t)->-n.value(r,t)); }
            if(take("!")) { Node n=unary(); return bounded((r,t)->n.value(r,t)>0?0:1); }
            Node n=atom();
            if(take("^")) { Node a=n,b=unary(); return bounded((r,t)->Math.pow(a.value(r,t),b.value(r,t))); }
            return n;
        } finally { depth--; }
    }
    private Node atom() {
        if(take("(")) { Node n=or(); if(!take(")"))throw error("Expected )"); return n; }
        space(); int start=position;
        if(position<text.length()&&(Character.isDigit(text.charAt(position))||text.charAt(position)=='.')) {
            while(position<text.length()&&(Character.isDigit(text.charAt(position))||text.charAt(position)=='.'))position++;
            if(position<text.length()&&text.charAt(position)=='e') {
                position++; if(position<text.length()&&(text.charAt(position)=='+'||text.charAt(position)=='-'))position++;
                while(position<text.length()&&Character.isDigit(text.charAt(position)))position++;
            }
            double v;
            try { v=Double.parseDouble(text.substring(start,position)); } catch(NumberFormatException e) {throw error("Invalid number");}
            if(!Double.isFinite(v))throw error("Number is not finite");
            return bounded((r,t)->v);
        }
        while(position<text.length()&&Character.isLetter(text.charAt(position)))position++;
        String name=text.substring(start,position);
        if(name.equals("r"))return bounded((r,t)->r);
        if(name.equals("theta"))return bounded((r,t)->t);
        if(name.equals("pi"))return bounded((r,t)->Math.PI);
        if(name.equals("e"))return bounded((r,t)->Math.E);
        if(name.isEmpty()||!take("("))throw error("Expected number, r, theta or function");
        List<Node> args=new ArrayList<>();
        args.add(or()); while(take(",")) { if(args.size()>=2)throw error("Too many arguments"); args.add(or()); }
        if(!take(")"))throw error("Expected )");
        Node a=args.get(0);
        if(args.size()==2) {
            Node b=args.get(1);
            return switch(name) {
                case "min" -> bounded((r,t)->Math.min(a.value(r,t),b.value(r,t)));
                case "max" -> bounded((r,t)->Math.max(a.value(r,t),b.value(r,t)));
                case "pow" -> bounded((r,t)->Math.pow(a.value(r,t),b.value(r,t)));
                default -> throw error("Unknown two-argument function: "+name);
            };
        }
        return switch(name) {
            case "sin"->bounded((r,t)->Math.sin(a.value(r,t)));
            case "cos"->bounded((r,t)->Math.cos(a.value(r,t)));
            case "tan"->bounded((r,t)->Math.tan(a.value(r,t)));
            case "abs"->bounded((r,t)->Math.abs(a.value(r,t)));
            case "sqrt"->bounded((r,t)->Math.sqrt(a.value(r,t)));
            case "floor"->bounded((r,t)->Math.floor(a.value(r,t)));
            case "ceil"->bounded((r,t)->Math.ceil(a.value(r,t)));
            default->throw error("Unknown function: "+name);
        };
    }
}
