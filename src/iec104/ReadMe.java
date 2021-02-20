package iec104;

public class ReadMe {
    /*Реализация Slave-устройстрва протокола МЭК-104
    
    Все каналы (объекты информации-InformationObject) хранятся в колеекции Vector класса Heap
    Объект информации сожержит адрес объекта информации и элемент информации.
    Элемент информации, соответсвует классификации МЭК-104 [1] стр 18
    
    Библиотека сомостоятельно поддерживает соединение с master-станцией. Текущее сосотяние в IecAPDU.currentState
    При currentState == 4 можно в очередь отправки IecAPDU.transmitOrder добавлять пакеты (предварительно желательно убеждаться, что очередь пуста)
    
    Метод TaskManager.run срабатывает с заданной периодичностью. Он должен проверять изменились ли данные в элементах информации 
    и если надо формировать пакет и закидывать его в очередь на отправку.
    
    Пока реализован только один тип ElementType11. Проверки на измения значений не производится. Отправляются всегда все элементом одним пакетом.
    
    Есть недоработка обработки подтвержления master-станции после того, как slave завершает активацию (причина передачи 10 после C_IC_NA_1 [1] стр.90
    Пока в качестве подтверждения "ловится" только s-тип, а по стандарту может быть и i-тип.
    
    
     */
}