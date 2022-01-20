package com.example.todaybap.retrofit.model

data class NaverGCDTO(
    val status: Status?,
    val results: List<Order?>
)

/**
 * @param code: 코드 정보
 * @param name: 변환 작업 이름
 */
data class Status(
    val code: Int?,
    val name: String?,
    val message: String?
)

/**
 * @param name: 코드 타입 ex) legal to 법정동, adm to 행정동...
 * @param code: 코드 정보
 * @param region: 지역 명칭 정보
 * @param land: 상세주소 정보
 */
data class Order(
    val name: String?,
    val code: Code?,
    val region: Region?,
    val land: Land?
)

/**
 * @param id: 코드값
 * @param type: 코드 타입 ex) L to 법정동, A to 행정동, S to 동일법정동 이름 존재하는 행정동...
 * @param mappingId: id와 관련된 매핑 코드 ex) 법정 코드의 매핑된 네이버 동 코드 또는 행정 코드의 매핑된 네이버 동 코드
 */
data class Code(
    val id: String?,
    val type: String?,
    val mappingId: String?
)

/**
 * @param area0: 국가 단위 정보
 * @param area1: 시/도 단위 정보
 * @param area2: 시/군/구 단위 정보
 * @param area3: 읍/면/동 단위 정보
 * @param area4: 리 단위 정보
 */
data class Region(
    val area0: Area?,
    val area1: Area?,
    val area2: Area?,
    val area3: Area?,
    val area4: Area?
)

/**
 * @param type: 지번주소의 경우 지적 타입 ex) 1 to 일반토지, 2 to 산.., 도로명주소의 경우 reserved
 * @param number1: 지번주소의 경우 토지 본번호, 도로명주소의 경우 상세주소
 * @param number2: 지번주소의 경우 토지 부번호, 도로명주소인 경우 reserved
 * @param addition0: 추가 정보) 지번주소의 경우 reserved, 도로명주소의 경우 건물정보
 * @param addition1: 추가 정보) 지번주소의 경우 reserved, 도로명주소의 경우 우편번호
 * @param addition2: 추가 정보) 지번주소의 경우 reserved, 도로명주소의 경우 도로코드
 * @param addition3: reserved
 * @param addition4: reserved
 * @param addition5: reserved
 * @param name: 상세 명칭) 지번주소의 경우 reserved, 도로명주소의 경우 도로명
 * @param coords: 지적 구역에 관계된 좌표(reserved)
 */
data class Land(
    val type: String?,
    val number1: String?,
    val number2: String?,
    val addition0: Addition?,
    val addition1: Addition?,
    val addition2: Addition?,
    val addition4: Addition?,
    val addition5: Addition?,
    val name: String?,
    val coords: Center?
)

data class Area(
    val name: String?,
    val coords: Coords?
)

data class Addition(
    val type: String?,
    val value: String?
)

data class Coords(
    val center: Center?
)

data class Center(
    val crs: String?,
    val x: Double?,
    val y: Double?
)