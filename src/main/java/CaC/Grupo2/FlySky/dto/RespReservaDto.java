package CaC.Grupo2.FlySky.dto;

import CaC.Grupo2.FlySky.entity.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespReservaDto {
    private ReservaDto reserva;
    private String mensaje;
}
