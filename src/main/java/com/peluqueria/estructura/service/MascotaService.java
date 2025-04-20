package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    @Autowired
    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    public List<Mascota> findAll() {
        return mascotaRepository.findAll();
    }

    public Optional<Mascota> findById(String id) {
        return mascotaRepository.findById(id);
    }

    public List<Mascota> findByClienteId(String clienteId) {
        return mascotaRepository.findByClienteId(clienteId);
    }

    public List<Mascota> findByClienteUsuarioUsername(String username) {
        return mascotaRepository.findByClienteUsuarioUsername(username);
    }

    public Optional<Mascota> findByIdAndClienteUsuarioUsername(String id, String username) {
        return mascotaRepository.findByIdAndClienteUsuarioUsername(id, username);
    }

    public Mascota save(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public void deleteById(String id) {
        mascotaRepository.deleteById(id);
    }

    public void saveFoto(String id, byte[] foto) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
        mascota.setFoto(foto);
        mascotaRepository.save(mascota);
    }

    public byte[] getFoto(String id) {
        Mascota mascota = mascotaRepository.findById(id).orElse(null);
        return (mascota != null) ? mascota.getFoto() : null;
    }
}
