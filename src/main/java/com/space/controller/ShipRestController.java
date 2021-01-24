package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/rest/ships")

public class ShipRestController {

    @Autowired
    private ShipService shipService;

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long shipId) {
        if (shipId == null || shipId <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Ship ship = this.shipService.getById(shipId);

        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> createShip(@RequestBody(required = false) Ship ship) {

        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null ||
                ship.getProdDate() == null || ship.getSpeed() == null || ship.getCrewSize() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getName().length() > 50 || ship.getPlanet().length() > 50 ||
                ship.getName().equals("") || ship.getPlanet().equals("")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        double roundedSpeed = (double) Math.round(ship.getSpeed() * 100) / 100;
        if (roundedSpeed < 0.01 || roundedSpeed > 0.99) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ship.setSpeed(roundedSpeed);

        if (ship.getCrewSize() < 1 || ship.getCrewSize() > 9999) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getProdDate().getTime() < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getProdDate().getYear()+1900 > 3019 || ship.getProdDate().getYear()+1900 < 2800) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }

        double k = ship.getUsed() ? 0.5 : 1;
        double rating = 80 * roundedSpeed * k / (3019 - (ship.getProdDate().getYear()+1900) + 1);
        double roundedRating = (double) Math.round(rating * 100) / 100;
        ship.setRating(roundedRating);

        this.shipService.save(ship);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long shipId,
                                           @RequestBody(required = false) Ship newShip){

        if (shipId == null || shipId <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Ship oldShip = this.shipService.getById(shipId);

        if (oldShip == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (newShip.getName() != null) {
            if (newShip.getName().equals("") || newShip.getName().length() > 50)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            oldShip.setName(newShip.getName());
        }

        if (newShip.getPlanet() != null) {
            if (newShip.getPlanet().equals("") || newShip.getPlanet().length() > 50)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            oldShip.setPlanet(newShip.getPlanet());
        }

        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }

        if (newShip.getProdDate() != null) {
            Date date = newShip.getProdDate();
            if (date.getYear()+1900 < 2800 || date.getYear() > 3019)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            oldShip.setProdDate(date);
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
        }

        if (newShip.getSpeed() != null) {
            double roundedSpeed = (double) Math.round(newShip.getSpeed() * 100) / 100;
            if (roundedSpeed < 0.01 || roundedSpeed > 0.99)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            oldShip.setSpeed(roundedSpeed);
        }

        if (newShip.getCrewSize() != null) {
            if (newShip.getCrewSize() < 1 || newShip.getCrewSize() > 9999)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            oldShip.setCrewSize(newShip.getCrewSize());
        }

        double k = oldShip.getUsed() ? 0.5 : 1;
        double rating = 80 * oldShip.getSpeed() * k / (3019 - (oldShip.getProdDate().getYear()+1900) + 1);
        double roundedRating = (double) Math.round(rating * 100) / 100;
        oldShip.setRating(roundedRating);

        this.shipService.save(oldShip);

        return new ResponseEntity<>(oldShip, HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Ship ship = this.shipService.getById(id);

        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        this.shipService.delete(id);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<Ship>> getAllShip(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
            ) {
        List<Ship> ships = this.shipService.getAll();

        if(ships.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Iterator<Ship> it = ships.iterator();
        while (it.hasNext()) {
            Ship ship = it.next();
            if (name != null) {
                if (!ship.getName().contains(name)) {
                    it.remove();
                    continue;
                }
            }
            if (planet != null) {
                if (!ship.getPlanet().contains(planet)) {
                    it.remove();
                    continue;
                }
            }
            if (shipType != null) {
                if (!ship.getShipType().equals(shipType)) {
                    it.remove();
                    continue;
                }
            }
            if (after != null) {
                Date dateAfter = new Date(after);
                if (ship.getProdDate().before(dateAfter)) {
                    it.remove();
                    continue;
                }
            }
            if (before != null) {
                Date dateBefore = new Date(before);
                if (ship.getProdDate().after(dateBefore)) {
                    it.remove();
                    continue;
                }
            }
            if (isUsed != null) {
                if (!ship.getUsed().equals(isUsed)) {
                    it.remove();
                    continue;
                }
            }
            if (minSpeed != null) {
                if (ship.getSpeed() < minSpeed) {
                    it.remove();
                    continue;
                }
            }
            if (maxSpeed != null) {
                if (ship.getSpeed() > maxSpeed) {
                    it.remove();
                    continue;
                }
            }
            if (minCrewSize != null) {
                if (ship.getCrewSize() < minCrewSize) {
                    it.remove();
                    continue;
                }
            }
            if (maxCrewSize != null) {
                if (ship.getCrewSize() > maxCrewSize) {
                    it.remove();
                    continue;
                }
            }
            if (minRating != null) {
                if (ship.getRating() < minRating) {
                    it.remove();
                    continue;
                }
            }
            if (maxRating != null) {
                if (ship.getRating() > maxRating) {
                    it.remove();
                }
            }
        }

        if(order != null) {
            if (order == ShipOrder.ID) {
                ships.sort(Comparator.comparing(Ship::getId));
            }

            if (order == ShipOrder.SPEED) {
                ships.sort(Comparator.comparing(Ship::getSpeed));
            }

            if (order == ShipOrder.DATE) {
                ships.sort(Comparator.comparing(Ship::getProdDate));
            }

            if (order == ShipOrder.RATING) {
                ships.sort(Comparator.comparing(Ship::getRating));
            }
        }
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;
        int size = pageSize;
        if (pageNumber*pageSize + pageSize > ships.size()) {
            size = ships.size() - pageNumber*pageSize;
        }
        List<Ship> result = ships.subList(pageNumber*pageSize, pageNumber*pageSize + size);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Integer> getShipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {
        List<Ship> ships = this.shipService.getAll();

        if(ships.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Iterator<Ship> it = ships.iterator();
        while (it.hasNext()) {
            Ship ship = it.next();
            if (name != null) {
                if (!ship.getName().contains(name)) {
                    it.remove();
                    continue;
                }
            }
            if (planet != null) {
                if (!ship.getPlanet().contains(planet)) {
                    it.remove();
                    continue;
                }
            }
            if (shipType != null) {
                if (!ship.getShipType().equals(shipType)) {
                    it.remove();
                    continue;
                }
            }
            if (after != null) {
                Date dateAfter = new Date(after);
                if (ship.getProdDate().before(dateAfter)) {
                    it.remove();
                    continue;
                }
            }
            if (before != null) {
                Date dateBefore = new Date(before);
                if (ship.getProdDate().after(dateBefore)) {
                    it.remove();
                    continue;
                }
            }
            if (isUsed != null) {
                if (!ship.getUsed().equals(isUsed)) {
                    it.remove();
                    continue;
                }
            }
            if (minSpeed != null) {
                if (ship.getSpeed() < minSpeed) {
                    it.remove();
                    continue;
                }
            }
            if (maxSpeed != null) {
                if (ship.getSpeed() > maxSpeed) {
                    it.remove();
                    continue;
                }
            }
            if (minCrewSize != null) {
                if (ship.getCrewSize() < minCrewSize) {
                    it.remove();
                    continue;
                }
            }
            if (maxCrewSize != null) {
                if (ship.getCrewSize() > maxCrewSize) {
                    it.remove();
                    continue;
                }
            }
            if (minRating != null) {
                if (ship.getRating() < minRating) {
                    it.remove();
                    continue;
                }
            }
            if (maxRating != null) {
                if (ship.getRating() > maxRating) {
                    it.remove();
                }
            }
        }

        return new ResponseEntity<>(ships.size(), HttpStatus.OK);
    }


}
