package sample;


public class Particle {
   public Particle(double x, double y, double dir  ){
       this.x=x;
       this.y=y;
       this.direction=dir;
   }
    public Particle(double x, double y ){
        this.x=x;
        this.y=y;
        this.direction=0;
    }

    double x;
    double y;
    public   double direction;
}
