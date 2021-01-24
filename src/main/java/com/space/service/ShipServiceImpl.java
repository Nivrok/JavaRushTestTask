package com.space.service;

import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ShipServiceImpl implements ShipService{

    @Autowired
    ShipRepository shipRepository;

    @Override
    public Ship getById(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Ship ship) {
        shipRepository.save(ship);
    }

    @Override
    public void delete(Long id) {
        if (shipRepository.existsById(id))
            shipRepository.deleteById(id);
    }

    @Override
    public List<Ship> getAll() {
        return shipRepository.findAll();
    }
}
