package com.estudiHambre.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class MenuImagen {

    @Column(name = "imagen_url", nullable = false)
    private String url;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    public MenuImagen() {
    }

    public MenuImagen(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}