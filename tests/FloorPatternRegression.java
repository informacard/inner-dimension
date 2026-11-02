import com.innerdimension.projection.*;

public final class FloorPatternRegression {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static double eval(String s,double r,double t){return PolarExpression.compile(s).value(r,t);}
    private static void reject(String s){try{PolarExpression.compile(s);throw new AssertionError("Accepted: "+s);}catch(IllegalArgumentException expected){checks++;}}
    public static void main(String[] args) {
        check(eval("2+3*4",0,0)==14,"precedence");
        check(eval("-2^2",0,0)==-4,"unary/power precedence");
        check(eval("2^3^2",0,0)==512,"right-associative powers");
        check(eval("(-1) % 4",0,0)==3,"positive modulo for spiral seam");
        check(eval("min(3,max(1,2))",0,0)==2,"nested two-argument functions");
        check(eval("1e-2 + 0.99",0,0)==1,"scientific notation");
        check(eval("r>3 && abs(sin(theta))>0.2 || r<1",5,Math.PI/2)==1,"logical precedence");
        check(!PolarExpression.draws(PolarExpression.compile("1/0"),1,1),"infinite results are background");
        check(!PolarExpression.draws(PolarExpression.compile("sqrt(-1)"),1,1),"NaN is background");
        check(eval("1/0 > 0",0,0)==0,"nonfinite comparison rejected");
        check(FloorPatternSettings.DEFAULT.mode()==FloorPatternSettings.Mode.TILE,"default is plain tiling");
        check(FloorPatternSettings.DEFAULT.preserveCenter(),"default preserves centre");
        reject("");reject("sin()");reject("foo(r)");reject("r; System.exit(0)");reject("r > > 2");reject("min(r)");reject("sin(r,theta)");reject("1e999");
        reject("(".repeat(40)+"r"+")".repeat(40));reject("r+".repeat(120)+"r");reject("1".repeat(513));
        var rings=new FloorPatternSettings(FloorPatternSettings.Mode.RINGS,"",true,"","").compile();
        var gaps=new FloorPatternSettings(FloorPatternSettings.Mode.BROKEN_RINGS,"",true,"","").compile();
        for(int i=0;i<320;i++) {
            double r=i/10.0;
            check(PolarExpression.draws(rings,r,0)==PolarExpression.draws(rings,r,Math.PI/2),"rings rotational symmetry");
            check(!PolarExpression.draws(gaps,r,0),"east gap persists at every radius");
            check(!PolarExpression.draws(gaps,r,Math.PI),"west gap persists at every radius");
            check(PolarExpression.draws(gaps,r,Math.PI/2)==PolarExpression.draws(rings,r,Math.PI/2),"ungapped arcs preserve ring width");
        }
        var spiral=new FloorPatternSettings(FloorPatternSettings.Mode.SPIRAL,"",false,"","").compile();
        for(int i=0;i<100;i++) {
            double r=i*.37;
            check(PolarExpression.draws(spiral,r,-Math.PI)==PolarExpression.draws(spiral,r,Math.PI),"spiral angle seam");
        }
        for(var mode:FloorPatternSettings.Mode.values())if(mode!=FloorPatternSettings.Mode.TILE) {
            var node=new FloorPatternSettings(mode,mode.expression,false,"","").compile();
            int drawn=0;
            for(int x=-24;x<40;x++)for(int z=-24;z<40;z++)if(FloorPatternSettings.foreground(node,x,z))drawn++;
            check(drawn>0&&drawn<4096,"preset neither blank nor solid: "+mode);
        }
        check(FloorPatternSettings.central(0,0)&&FloorPatternSettings.central(15,15),"inclusive centre bounds");
        check(!FloorPatternSettings.central(-1,8)&&!FloorPatternSettings.central(16,8),"outside centre bounds");
        System.out.println("FloorPatternRegression passed: "+checks+" checks");
    }
}
