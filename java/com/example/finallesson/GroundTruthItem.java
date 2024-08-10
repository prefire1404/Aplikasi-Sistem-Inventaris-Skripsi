package com.example.finallesson;

public class GroundTruthItem {
    String id;
    String nama;
    String kategori;
    String kondisi;
    String jumlah;

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getKategori() {
        return kategori;
    }

    public String getKondisi() {
        return kondisi;
    }

    public String getJumlah() {return jumlah;}

    public void setId(String id) {
        this.id = id;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public void setKondisi(String kondisi) {
        this.kondisi = kondisi;
    }

    public void setJumlah(String jumlah) {this.jumlah = jumlah;}

    public GroundTruthItem(String id, String nama, String kategori, String kondisi, String jumlah) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.kondisi = kondisi;
        this.jumlah = jumlah;
    }
}
