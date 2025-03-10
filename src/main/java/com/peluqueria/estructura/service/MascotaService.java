package com.peluqueria.estructura.service;

import com.peluqueria.estructura.entity.Mascota;
import com.peluqueria.estructura.repository.MascotaRepository;
import com.peluqueria.estructura.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;

    public MascotaService(MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
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

    public List<Mascota> getMascotasByUsuario(String username) {
        return mascotaRepository.findByClienteUsuarioUsername(username);
    }

    public Mascota getMascotaByIdAndUsuario(Long id, String username) {
        return mascotaRepository.findByIdAndClienteUsuarioUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));
    }

    public Mascota createMascota(Mascota mascota, String username) {
        mascota.setCliente(clienteRepository.findByUsuarioUsername(username)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado")));
        return mascotaRepository.save(mascota);
    }

    public Mascota updateMascota(Long id, Mascota mascota, String username) {
        Mascota existingMascota = getMascotaByIdAndUsuario(id, username);
        existingMascota.setNombre(mascota.getNombre());
        existingMascota.setTipo(mascota.getTipo());
        existingMascota.setRaza(mascota.getRaza());
        existingMascota.setEdad(mascota.getEdad());
        return mascotaRepository.save(existingMascota);
    }

    public void deleteMascota(Long id, String username) {
        Mascota mascota = getMascotaByIdAndUsuario(id, username);
        mascotaRepository.delete(mascota);
    }
}

