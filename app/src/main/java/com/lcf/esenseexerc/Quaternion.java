package com.lcf.esenseexerc;

public class Quaternion {

    private double[] vec;

    public double[] getVec() {
        return vec;
    }

    public void setVec(double[] vec) {
        this.vec = vec;
    }

    public Quaternion(double s, double x, double y, double z) {
        this.vec = new double[4];
        this.vec[0] = s;
        this.vec[1] = x;
        this.vec[2] = y;
        this.vec[3] = z;
    }

    public static Quaternion quan_Mult(Quaternion q1, Quaternion q2) {
        double[] q = q1.getVec();
        double[] p = q2.getVec();

        double s = q[0]*p[0] - q[1]*p[1] - q[2]*p[2] - q[3]*p[3];
        double x = q[0]*p[1] + q[1]*p[0] + q[2]*p[3] - q[3]*p[2];
        double y = q[0]*p[2] - q[1]*p[3] + q[2]*p[0] + q[3]*p[1];
        double z = q[0]*p[3] + q[1]*p[2] - q[2]*p[1] + q[3]*p[0];

        Quaternion rtn = new Quaternion(s, x, y, z);

        return rtn;
    }

     public static Quaternion normalize(Quaternion q) {
        double[] nor = q.getVec();
        double mod = Math.sqrt(nor[0]*nor[0] + nor[1]*nor[1] + nor[2]*nor[2] + nor[3]*nor[3]);
        Quaternion rtn = new Quaternion(nor[0]/mod, nor[1]/mod, nor[2]/mod, nor[3]/mod);
        return rtn;
    }



}
