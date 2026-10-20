package com.estudiHambre.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.estudiHambre.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public ImagenSubida subirImagen(MultipartFile imagen) {
        return subirImagen(imagen, "estudihambre/establecimientos");
    }

    public ImagenSubida subirImagen(MultipartFile imagen, String carpeta) {

        validarImagen(imagen);

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    imagen.getBytes(),
                    ObjectUtils.asMap(
                            "folder", carpeta,
                            "resource_type", "image"
                    )
            );

            String url = (String) resultado.get("secure_url");
            String publicId = (String) resultado.get("public_id");

            return new ImagenSubida(url, publicId);

        } catch (IOException e) {
            throw new ReglaNegocioException(
                    "No fue posible subir la imagen a Cloudinary"
            );
        }
    }

    public void eliminarImagen(String publicId) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image")
            );

        } catch (IOException e) {
            throw new ReglaNegocioException(
                    "No fue posible eliminar la imagen de Cloudinary"
            );
        }
    }

    private void validarImagen(MultipartFile imagen) {

        if (imagen == null || imagen.isEmpty()) {
            throw new ReglaNegocioException(
                    "Debes seleccionar una imagen"
            );
        }

        if (imagen.getSize() > TAMANO_MAXIMO) {
            throw new ReglaNegocioException(
                    "La imagen no puede superar los 5 MB"
            );
        }

        if (!TIPOS_PERMITIDOS.contains(imagen.getContentType())) {
            throw new ReglaNegocioException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }
    }

    public record ImagenSubida(
            String url,
            String publicId
    ) {
    }
}