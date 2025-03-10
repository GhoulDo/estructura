package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.repository.MascotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    public List<Mascota> listarMascotas() {
        return mascotaRepository.findAll();
    }

    public Optional<Mascota> obtenerMascotaPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    public Mascota guardarMascota(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public void eliminarMascota(Long id) {
        mascotaRepository.deleteById(id);
    }

    public List<Mascota> getAllMascotas() {
        return mascotaRepository.findAll();
    }

    public Mascota createMascota(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }
}

