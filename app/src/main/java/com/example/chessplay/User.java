package com.example.chessplay;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {

    private Integer bwin;
    private Integer bLose;
    private Integer wwin;
    private Integer wLose;
    private Integer sWin;
    private Integer sLose;
    private Integer mWin;
    private Integer mLose;
    private Integer eWin;
    private Integer eLose;
    private Integer semiClosedStart;
    private Integer closedStart;
    private Integer kingStart;
    private Integer queenStart;

    public Integer getBwin() {
        return bwin;
    }

    public void setBwin(Integer bwin) {
        this.bwin = bwin;
    }

    public Integer getbLose() {
        return bLose;
    }

    public void setbLose(Integer bLose) {
        this.bLose = bLose;
    }

    public Integer getWwin() {
        return wwin;
    }

    public void setWwin(Integer wwin) {
        this.wwin = wwin;
    }

    public Integer getwLose() {
        return wLose;
    }

    public void setwLose(Integer wLose) {
        this.wLose = wLose;
    }

    public Integer getsWin() {
        return sWin;
    }

    public void setsWin(Integer sWin) {
        this.sWin = sWin;
    }

    public Integer getsLose() {
        return sLose;
    }

    public void setsLose(Integer sLose) {
        this.sLose = sLose;
    }

    public Integer getmWin() {
        return mWin;
    }

    public void setmWin(Integer mWin) {
        this.mWin = mWin;
    }

    public Integer getmLose() {
        return mLose;
    }

    public void setmLose(Integer mLose) {
        this.mLose = mLose;
    }

    public Integer geteWin() {
        return eWin;
    }

    public void seteWin(Integer eWin) {
        this.eWin = eWin;
    }

    public Integer geteLose() {
        return eLose;
    }

    public void seteLose(Integer eLose) {
        this.eLose = eLose;
    }

    public Integer getSemiClosedStart() {
        return semiClosedStart;
    }

    public void setSemiClosedStart(Integer semiClosedStart) {
        this.semiClosedStart = semiClosedStart;
    }

    public Integer getClosedStart() {
        return closedStart;
    }

    public void setClosedStart(Integer closedStart) {
        this.closedStart = closedStart;
    }

    public Integer getKingStart() {
        return kingStart;
    }

    public void setKingStart(Integer kingStart) {
        this.kingStart = kingStart;
    }

    public Integer getQueenStart() {
        return queenStart;
    }

    public void setQueenStart(Integer queenStart) {
        this.queenStart = queenStart;
    }

    @Override
    public String toString() {
        return "User{" +
                "bwin=" + bwin +
                ", bLose=" + bLose +
                ", wwin=" + wwin +
                ", wLose=" + wLose +
                ", sWin=" + sWin +
                ", sLose=" + sLose +
                ", mWin=" + mWin +
                ", mLose=" + mLose +
                ", eWin=" + eWin +
                ", eLose=" + eLose +
                ", semiClosedStart=" + semiClosedStart +
                ", closedStart=" + closedStart +
                ", kingStart=" + kingStart +
                ", queenStart=" + queenStart +
                '}';
    }
}
