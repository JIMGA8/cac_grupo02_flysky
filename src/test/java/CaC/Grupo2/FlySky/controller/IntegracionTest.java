package CaC.Grupo2.FlySky.controller;

import CaC.Grupo2.FlySky.dto.request.PagoDto;
import CaC.Grupo2.FlySky.dto.request.SolHistorialDto;
import CaC.Grupo2.FlySky.dto.request.SolVentasDiariasDto;
import CaC.Grupo2.FlySky.dto.response.ErrorDto;
import CaC.Grupo2.FlySky.dto.request.ReservaDto;
import CaC.Grupo2.FlySky.dto.response.RtaHistorialDto;
import CaC.Grupo2.FlySky.dto.response.VueloDtoSA;
import CaC.Grupo2.FlySky.entity.Pago.TipoPago;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class IntegracionTest {

    @Autowired
    MockMvc mockMvc;



    @Test
    void testGetAllVuelos() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/getAllVuelos"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].origen").value("Mendoza"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].destino").value("Bs. As"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].precio").value(175.5))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].aerolinea").value("Jestmar"))
                .andReturn();

        assertEquals("application/json",mvcResult.getResponse().getContentType());
    }

    @Test
    void testReservaVuelo() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        String rutaArchivo = "src/test/java/CaC/Grupo2/FlySky/controller/jsonReservaVuelo.json";

        File archivoJson = new File(rutaArchivo);

        ReservaDto reservaDto = objectMapper.readValue(archivoJson, ReservaDto.class);

        String jsonPayload = objectMapper.writeValueAsString(reservaDto);

        MvcResult mvcResult = mockMvc.perform(post("/reservaVuelo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        assertEquals("application/json",mvcResult.getResponse().getContentType());
        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseContent);
        String mensaje = jsonResponse.get("mensaje").asText();
        assertEquals("Su reserva se realizo con exito... Tienes 10 minutos para realizar el pago",mensaje);
    }

    @Test
    void testReservaVueloNoExistente() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        ErrorDto errorDto = new ErrorDto("No se encontro el vuelo con el ID especificado");

        String rutaArchivo = "src/test/java/CaC/Grupo2/FlySky/controller/jsonReservaVueloNoExistete.json";

        File archivoJson = new File(rutaArchivo);

        ReservaDto reservaDto = objectMapper.readValue(archivoJson, ReservaDto.class);

        ObjectWriter writer = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE,false)
                .writer();

        String jsonPayload = objectMapper.writeValueAsString(reservaDto);
        String errorExpected = writer.writeValueAsString(errorDto);

        MvcResult mvcResult = mockMvc.perform(post("/reservaVuelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                        .andDo(print())
                        .andReturn();

        assertEquals(errorExpected,mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testPagarReserva() throws Exception{
        PagoDto pagoDtoIn = new PagoDto(5, TipoPago.efectivo,150);
        String rta = "Reserva pagada exitosamente";

        //Transformo los objetos a Json
        ObjectWriter objToJson = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE,false)
                .writer();

        String jsonPayloadEntrada = objToJson.writeValueAsString(pagoDtoIn);

        //Act
        MvcResult mvcResult = mockMvc.perform(post("/pagarReserva")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayloadEntrada))
                .andDo(print())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andReturn();

        //Assert
        assertEquals(rta,mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testvalidarConsultaVentaDiarias() throws Exception {
        SolVentasDiariasDto solventasDiariasDtoMock = new SolVentasDiariasDto();
        solventasDiariasDtoMock.setUsuarioIdAdministrador(1L);

        ObjectWriter writer = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE,false)
                .writer();

        String payloadDto = writer.writeValueAsString(solventasDiariasDtoMock);

        MvcResult mvcResult = mockMvc.perform(get("/getVentasDiarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadDto))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.totalVentas").value(1))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.ingresosGenerados").value(175.5))
                        .andReturn();
    }

    @Test
    void getHistorialTest() throws Exception {
        //Valor Recibido DTO
        SolHistorialDto solHistorialDtoI = new SolHistorialDto(2L,3L);

        List<VueloDtoSA> vueloDtoSAI = new ArrayList<>();
        vueloDtoSAI.add(new VueloDtoSA("Mendoza","Bs. As","Jestmar",new Date(123,8,9)));
        vueloDtoSAI.add(new VueloDtoSA("Tierra del Fuego","Bs. As","AirlineZ",new Date(123,8,10)));

        RtaHistorialDto expected = new RtaHistorialDto("Historial y Preferencias de Vuelo del Cliente Michael Johnson",vueloDtoSAI);

        //Transformo los objetos a Json
        ObjectWriter objToJson = new ObjectMapper()
                .configure(SerializationFeature.WRAP_ROOT_VALUE,false)
                .writer();

        String jsonPayloadEntrada = objToJson.writeValueAsString(solHistorialDtoI);
        String JsonPayLoadSalida = objToJson.writeValueAsString(expected);

        //Act
        MvcResult mvcResult = mockMvc.perform(get("/getHistorial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayloadEntrada))
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        //Assert
       assertEquals(JsonPayLoadSalida,mvcResult.getResponse().getContentAsString());
    }


}
