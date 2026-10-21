
package com.estudiHambre.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    ImagenSubida subirImagen(MultipartFile imagen);

    ImagenSubida subirImagen(MultipartFile imagen, String carpeta);

    void eliminarImagen(String publicId);

    record ImagenSubida(
            String url,
            String publicId
    ) {
    }
}
