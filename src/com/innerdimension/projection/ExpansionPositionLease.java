package com.innerdimension.projection;
import net.minecraft.*;
import java.util.*;
/** Holds predicted landings during the short visual expansion, independent of arbitration. */
public final class ExpansionPositionLease {
    private static final Map<UUID,Lease> LEASES=new HashMap<>();
    private static final class Lease {
        final class_1297 actor;final boolean gravity;final RoomProjectionEntity shell,interior;
        ProjectionSafetySearch.Point target;
        Lease(ProjectionSafety.Move m,RoomProjectionEntity shell,RoomProjectionEntity interior,boolean gravity){actor=m.entity();target=m.to();this.shell=shell;this.interior=interior;this.gravity=gravity;}
        void pin(){
            actor.method_5875(true);actor.method_18799(class_243.field_1353);actor.field_6017=0;
            if(Math.abs(actor.method_23317()-target.x())>1e-6||Math.abs(actor.method_23318()-target.y())>1e-6||Math.abs(actor.method_23321()-target.z())>1e-6) {
                if(actor instanceof class_3222 p)p.field_13987.method_14363(target.x(),target.y(),target.z(),p.method_36454(),p.method_36455());
                else actor.method_5814(target.x(),target.y(),target.z());
            }
        }
        void release(){
            actor.method_5875(gravity);
            if(actor instanceof class_3222) {
                double support=ProjectionEnvironment.supportHeight(actor.method_73183(),actor.method_5829(),actor.method_23318()-.125,actor.method_23318()+.01);
                org.slf4j.LoggerFactory.getLogger("inner_dimension.projection").info("Expansion landing released: player={} position={} projectedSupport={} ghostInterior={}",actor.method_5667(),actor.method_73189(),support,interior.contestGhost());
            }
        }
    }
    private ExpansionPositionLease(){}
    public static void hold(List<ProjectionSafety.Move> moves,RoomProjectionEntity shell,RoomProjectionEntity interior){
        for(var move:moves){
            var old=LEASES.get(move.entity().method_5667());
            var lease=new Lease(move,shell,interior,old==null?RoomProjectionManager.originalNoGravity(move.entity()):old.gravity);
            LEASES.put(move.entity().method_5667(),lease);lease.pin();
        }
    }
    public static void relocated(List<ProjectionSafety.Move> moves){for(var m:moves){var lease=LEASES.get(m.entity().method_5667());if(lease!=null)lease.target=m.to();}}
    public static void releaseCaster(RoomProjectionEntity shell) {
        var lease=LEASES.get(shell.owner());
        if(lease!=null&&lease.shell==shell){LEASES.remove(shell.owner());lease.release();}
    }
    public static boolean holdMovement(class_3222 player,class_2828 packet){
        var lease=LEASES.get(player.method_5667());if(lease==null)return false;
        float yaw=packet.method_12271(player.method_36454()),pitch=packet.method_12270(player.method_36455());
        if(!Float.isFinite(yaw)||!Float.isFinite(pitch))return false;
        player.field_13987.method_14363(lease.target.x(),lease.target.y(),lease.target.z(),yaw,pitch);return true;
    }
    public static void tick(){
        LEASES.entrySet().removeIf(e->{
            var l=e.getValue();
            if(!l.actor.method_5805()||l.actor.method_31481()||l.actor.method_73183()!=l.shell.method_73183()
                ||l.shell.method_31481()||l.shell.phase()==ProjectionAnimation.RETRACTING
                ||l.shell.phase()==ProjectionAnimation.STABLE&&l.interior.phase()==ProjectionAnimation.STABLE){l.release();return true;}
            l.pin();return false;
        });
    }
    public static void clear(){for(var l:LEASES.values())l.release();LEASES.clear();}
}
