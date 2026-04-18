package com.mosait.ems.core.database.mapper

import com.mosait.ems.core.database.entity.MissionEntity
import com.mosait.ems.core.database.entity.PatientEntity
import com.mosait.ems.core.database.entity.InitialAssessmentEntity
import com.mosait.ems.core.database.entity.DiagnosisEntity
import com.mosait.ems.core.database.entity.InfectionProtocolEntity
import com.mosait.ems.core.database.entity.InjuryEntity
import com.mosait.ems.core.database.entity.VitalSignEntity
import com.mosait.ems.core.database.entity.MeasuresEntity
import com.mosait.ems.core.database.entity.MissionResultEntity
import com.mosait.ems.core.database.entity.TransportRefusalEntity
import com.mosait.ems.core.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ---- Mission ----

fun MissionEntity.toDomain(): Mission = Mission(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = MissionStatus.valueOf(status),
    einsatzDatum = einsatzDatum,
    einsatzNummer = einsatzNummer,
    einsatzArt = EinsatzArt.valueOf(einsatzArt),
    rettungsMittel = RettungsMittel.valueOf(rettungsMittel),
    fahrzeugKennung = fahrzeugKennung,
    funkKennung = funkKennung,
    einsatzOrtStrasse = einsatzOrtStrasse,
    einsatzOrtPlz = einsatzOrtPlz,
    einsatzOrtOrt = einsatzOrtOrt,
    einsatzOrtZusatz = einsatzOrtZusatz,
    transportZiel = transportZiel,
    personal = parsePersonalJson(personalJson),
    kmAnfang = kmAnfang,
    kmEnde = kmEnde,
    zeitAlarm = zeitAlarm,
    zeitAbfahrt = zeitAbfahrt,
    zeitAnkunftEinsatzort = zeitAnkunftEinsatzort,
    zeitAbfahrtEinsatzort = zeitAbfahrtEinsatzort,
    zeitAnkunftKrankenhaus = zeitAnkunftKrankenhaus,
    zeitFreimeldung = zeitFreimeldung,
    zeitEnde = zeitEnde,
    sondersignalZumEinsatz = sondersignalZumEinsatz,
    sondersignalPatientenfahrt = sondersignalPatientenfahrt,
    bemerkungen = bemerkungen
)

fun Mission.toEntity(): MissionEntity = MissionEntity(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    status = status.name,
    einsatzDatum = einsatzDatum,
    einsatzNummer = einsatzNummer,
    einsatzArt = einsatzArt.name,
    rettungsMittel = rettungsMittel.name,
    fahrzeugKennung = fahrzeugKennung,
    funkKennung = funkKennung,
    einsatzOrtStrasse = einsatzOrtStrasse,
    einsatzOrtPlz = einsatzOrtPlz,
    einsatzOrtOrt = einsatzOrtOrt,
    einsatzOrtZusatz = einsatzOrtZusatz,
    transportZiel = transportZiel,
    personalJson = personalToJson(personal),
    kmAnfang = kmAnfang,
    kmEnde = kmEnde,
    zeitAlarm = zeitAlarm,
    zeitAbfahrt = zeitAbfahrt,
    zeitAnkunftEinsatzort = zeitAnkunftEinsatzort,
    zeitAbfahrtEinsatzort = zeitAbfahrtEinsatzort,
    zeitAnkunftKrankenhaus = zeitAnkunftKrankenhaus,
    zeitFreimeldung = zeitFreimeldung,
    zeitEnde = zeitEnde,
    sondersignalZumEinsatz = sondersignalZumEinsatz,
    sondersignalPatientenfahrt = sondersignalPatientenfahrt,
    bemerkungen = bemerkungen
)

private fun parsePersonalJson(json: String): List<PersonalEntry> {
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            PersonalEntry(
                name = obj.optString("name", ""),
                rolle = PersonalRolle.valueOf(obj.optString("rolle", "RETTUNGSSANITAETER"))
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun personalToJson(personal: List<PersonalEntry>): String {
    val array = JSONArray()
    personal.forEach { entry ->
        val obj = JSONObject().apply {
            put("name", entry.name)
            put("rolle", entry.rolle.name)
        }
        array.put(obj)
    }
    return array.toString()
}

// ---- Patient ----

fun PatientEntity.toDomain(): Patient = Patient(
    id = id,
    missionId = missionId,
    createdAt = createdAt,
    nachname = nachname,
    vorname = vorname,
    geburtsdatum = geburtsdatum,
    geschlecht = Geschlecht.valueOf(geschlecht),
    krankenkasse = krankenkasse,
    versichertenNummer = versichertenNummer,
    versichertenStatus = versichertenStatus,
    kostentraegerKennung = kostentraegerKennung,
    betriebsstaetteNummer = betriebsstaetteNummer,
    arztNummer = arztNummer,
    strasse = strasse,
    plz = plz,
    ort = ort,
    telefon = telefon
)

fun Patient.toEntity(): PatientEntity = PatientEntity(
    id = id,
    missionId = missionId,
    createdAt = createdAt,
    nachname = nachname,
    vorname = vorname,
    geburtsdatum = geburtsdatum,
    geschlecht = geschlecht.name,
    krankenkasse = krankenkasse,
    versichertenNummer = versichertenNummer,
    versichertenStatus = versichertenStatus,
    kostentraegerKennung = kostentraegerKennung,
    betriebsstaetteNummer = betriebsstaetteNummer,
    arztNummer = arztNummer,
    strasse = strasse,
    plz = plz,
    ort = ort,
    telefon = telefon
)

// ---- InitialAssessment ----

fun InitialAssessmentEntity.toDomain(): InitialAssessment = InitialAssessment(
    id = id,
    patientId = patientId,
    notfallgeschehen = notfallgeschehen,
    bewusstseinslage = Bewusstseinslage.valueOf(bewusstseinslage),
    bewusstseinslageText = bewusstseinslageText,
    kreislaufSchock = kreislaufSchock,
    kreislaufStillstand = kreislaufStillstand,
    kreislaufReanimation = kreislaufReanimation,
    kreislaufSonstigesText = kreislaufSonstigesText,
    rrSystolisch = rrSystolisch,
    rrDiastolisch = rrDiastolisch,
    puls = puls,
    spO2 = spO2,
    atemfrequenz = atemfrequenz,
    blutzucker = blutzucker,
    temperatur = temperatur,
    messwertZeit = messwertZeit?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) },
    gcsAugen = gcsAugen,
    gcsVerbal = gcsVerbal,
    gcsMotorik = gcsMotorik,
    pupilleLinks = PupillenStatus.valueOf(pupilleLinks),
    pupilleRechts = PupillenStatus.valueOf(pupilleRechts),
    pupillenLichtreaktionLinks = pupillenLichtreaktionLinks,
    pupillenLichtreaktionRechts = pupillenLichtreaktionRechts,
    ekg = EkgRhythmus.valueOf(ekg),
    ekgSonstigesText = ekgSonstigesText,
    schmerzSkala = schmerzSkala,
    atmung = AtmungStatus.valueOf(atmung),
    atmungSonstigesText = atmungSonstigesText
)

fun InitialAssessment.toEntity(): InitialAssessmentEntity = InitialAssessmentEntity(
    id = id,
    patientId = patientId,
    notfallgeschehen = notfallgeschehen,
    bewusstseinslage = bewusstseinslage.name,
    bewusstseinslageText = bewusstseinslageText,
    kreislaufSchock = kreislaufSchock,
    kreislaufStillstand = kreislaufStillstand,
    kreislaufReanimation = kreislaufReanimation,
    kreislaufSonstigesText = kreislaufSonstigesText,
    rrSystolisch = rrSystolisch,
    rrDiastolisch = rrDiastolisch,
    puls = puls,
    spO2 = spO2,
    atemfrequenz = atemfrequenz,
    blutzucker = blutzucker,
    temperatur = temperatur,
    messwertZeit = messwertZeit?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    gcsAugen = gcsAugen,
    gcsVerbal = gcsVerbal,
    gcsMotorik = gcsMotorik,
    pupilleLinks = pupilleLinks.name,
    pupilleRechts = pupilleRechts.name,
    pupillenLichtreaktionLinks = pupillenLichtreaktionLinks,
    pupillenLichtreaktionRechts = pupillenLichtreaktionRechts,
    ekg = ekg.name,
    ekgSonstigesText = ekgSonstigesText,
    schmerzSkala = schmerzSkala,
    atmung = atmung.name,
    atmungSonstigesText = atmungSonstigesText
)

// ---- Diagnosis ----

fun DiagnosisEntity.toDomain(): Diagnosis = Diagnosis(
    id = id,
    patientId = patientId,
    keine = keine,
    selectedConditions = parseStringListJson(selectedConditionsJson),
    sonstigesTexte = parseStringMapJson(sonstigesTexteJson),
    freitext = freitext
)

fun Diagnosis.toEntity(): DiagnosisEntity = DiagnosisEntity(
    id = id,
    patientId = patientId,
    keine = keine,
    selectedConditionsJson = stringListToJson(selectedConditions),
    sonstigesTexteJson = stringMapToJson(sonstigesTexte),
    freitext = freitext
)

// ---- Injury ----

fun InjuryEntity.toDomain(): Injury = Injury(
    id = id,
    patientId = patientId,
    keine = keine,
    injuryTypes = parseStringListJson(injuryTypesJson).map { InjuryType.valueOf(it) },
    bodyRegions = parseBodyRegionsJson(bodyRegionsJson),
    kopfHalsFreitext = kopfHalsFreitext,
    freitext = freitext
)

fun Injury.toEntity(): InjuryEntity = InjuryEntity(
    id = id,
    patientId = patientId,
    keine = keine,
    injuryTypesJson = stringListToJson(injuryTypes.map { it.name }),
    bodyRegionsJson = bodyRegionsToJson(bodyRegions),
    kopfHalsFreitext = kopfHalsFreitext,
    freitext = freitext
)

// ---- VitalSign ----

fun VitalSignEntity.toDomain(): VitalSign = VitalSign(
    id = id,
    patientId = patientId,
    timestamp = timestamp,
    puls = puls,
    rrSystolisch = rrSystolisch,
    rrDiastolisch = rrDiastolisch,
    spO2 = spO2,
    atemfrequenz = atemfrequenz,
    blutzucker = blutzucker,
    temperatur = temperatur,
    ekg = ekg?.let { EkgRhythmus.valueOf(it) },
    gcs = gcs,
    schmerzSkala = schmerzSkala,
    bemerkung = bemerkung
)

fun VitalSign.toEntity(): VitalSignEntity = VitalSignEntity(
    id = id,
    patientId = patientId,
    timestamp = timestamp,
    puls = puls,
    rrSystolisch = rrSystolisch,
    rrDiastolisch = rrDiastolisch,
    spO2 = spO2,
    atemfrequenz = atemfrequenz,
    blutzucker = blutzucker,
    temperatur = temperatur,
    ekg = ekg?.name,
    gcs = gcs,
    schmerzSkala = schmerzSkala,
    bemerkung = bemerkung
)

// ---- Measures ----

fun MeasuresEntity.toDomain(): Measures = Measures(
    id = id,
    patientId = patientId,
    selectedMeasures = parseStringListJson(selectedMeasuresJson),
    sauerstoffLiterProMin = sauerstoffLiterProMin,
    medikamente = medikamente,
    sonstige = sonstige,
    sonstigesTexte = parseStringMapJson(sonstigesTexteJson),
    ersthelferMassnahmen = ErsthelferMassnahmen(
        suffizient = ersthelferSuffizient,
        insuffizient = ersthelferInsuffizient,
        aed = ersthelferAed,
        keine = ersthelferKeine
    )
)

fun Measures.toEntity(): MeasuresEntity = MeasuresEntity(
    id = id,
    patientId = patientId,
    selectedMeasuresJson = stringListToJson(selectedMeasures),
    sauerstoffLiterProMin = sauerstoffLiterProMin,
    medikamente = medikamente,
    sonstige = sonstige,
    sonstigesTexteJson = stringMapToJson(sonstigesTexte),
    ersthelferSuffizient = ersthelferMassnahmen.suffizient,
    ersthelferInsuffizient = ersthelferMassnahmen.insuffizient,
    ersthelferAed = ersthelferMassnahmen.aed,
    ersthelferKeine = ersthelferMassnahmen.keine
)

// ---- MissionResult ----

fun MissionResultEntity.toDomain(): MissionResult = MissionResult(
    id = id,
    patientId = patientId,
    zustandVerbessert = zustandVerbessert,
    zustandUnveraendert = zustandUnveraendert,
    zustandVerschlechtert = zustandVerschlechtert,
    transportNichtErforderlich = transportNichtErforderlich,
    patientLehntTransportAb = patientLehntTransportAb,
    notarztNachgefordert = notarztNachgefordert,
    notarztAbbestellt = notarztAbbestellt,
    hausarztInformiert = hausarztInformiert,
    todAmNotfallort = todAmNotfallort,
    todWaehrendTransport = todWaehrendTransport,
    sonstigesFreitext = sonstigesFreitext,
    nacaScore = nacaScore,
    uebergabeAn = uebergabeAn,
    uebergabeZeit = uebergabeZeit,
    wertsachen = wertsachen,
    verlaufsbeschreibung = verlaufsbeschreibung
)

fun MissionResult.toEntity(): MissionResultEntity = MissionResultEntity(
    id = id,
    patientId = patientId,
    zustandVerbessert = zustandVerbessert,
    zustandUnveraendert = zustandUnveraendert,
    zustandVerschlechtert = zustandVerschlechtert,
    transportNichtErforderlich = transportNichtErforderlich,
    patientLehntTransportAb = patientLehntTransportAb,
    notarztNachgefordert = notarztNachgefordert,
    notarztAbbestellt = notarztAbbestellt,
    hausarztInformiert = hausarztInformiert,
    todAmNotfallort = todAmNotfallort,
    todWaehrendTransport = todWaehrendTransport,
    sonstigesFreitext = sonstigesFreitext,
    nacaScore = nacaScore,
    uebergabeAn = uebergabeAn,
    uebergabeZeit = uebergabeZeit,
    wertsachen = wertsachen,
    verlaufsbeschreibung = verlaufsbeschreibung
)

// ---- JSON Helpers ----

private fun parseStringListJson(json: String): List<String> {
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun stringListToJson(list: List<String>): String {
    val array = JSONArray()
    list.forEach { array.put(it) }
    return array.toString()
}

private fun parseStringMapJson(json: String): Map<String, String> {
    return try {
        val obj = JSONObject(json)
        val map = mutableMapOf<String, String>()
        obj.keys().forEach { key -> map[key] = obj.optString(key, "") }
        map
    } catch (_: Exception) {
        emptyMap()
    }
}

private fun stringMapToJson(map: Map<String, String>): String {
    val obj = JSONObject()
    map.forEach { (key, value) -> obj.put(key, value) }
    return obj.toString()
}

private fun parseBodyRegionsJson(json: String): List<BodyRegionEntry> {
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            BodyRegionEntry(
                region = BodyRegion.valueOf(obj.getString("region")),
                severity = InjurySeverity.valueOf(obj.optString("severity", "LEICHT")),
                side = BodySide.valueOf(obj.optString("side", "MITTE"))
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun bodyRegionsToJson(regions: List<BodyRegionEntry>): String {
    val array = JSONArray()
    regions.forEach { entry ->
        val obj = JSONObject().apply {
            put("region", entry.region.name)
            put("severity", entry.severity.name)
            put("side", entry.side.name)
        }
        array.put(obj)
    }
    return array.toString()
}

// ---- InfectionProtocol ----

fun InfectionProtocolEntity.toDomain(): InfectionProtocol = InfectionProtocol(
    id = id,
    patientId = patientId,
    bekannteInfektionen = parseStringListJson(bekannteInfektionenJson),
    infektionFreitext = infektionFreitext,
    schutzHandschuhe = schutzHandschuhe,
    schutzMundschutz = schutzMundschutz,
    schutzSchutzbrille = schutzSchutzbrille,
    schutzSchutzkittel = schutzSchutzkittel,
    schutzFFP2 = schutzFFP2,
    schutzSonstiges = schutzSonstiges,
    expositionStichverletzung = expositionStichverletzung,
    expositionSchleimhaut = expositionSchleimhaut,
    expositionHautkontakt = expositionHautkontakt,
    expositionKeine = expositionKeine,
    fahrzeugDesinfiziert = fahrzeugDesinfiziert,
    geraeteDesinfiziert = geraeteDesinfiziert,
    waescheGewechselt = waescheGewechselt,
    desinfektionsmittel = desinfektionsmittel,
    desinfektionDurchgefuehrtVon = desinfektionDurchgefuehrtVon,
    bemerkungen = bemerkungen
)

fun InfectionProtocol.toEntity(): InfectionProtocolEntity = InfectionProtocolEntity(
    id = id,
    patientId = patientId,
    bekannteInfektionenJson = stringListToJson(bekannteInfektionen),
    infektionFreitext = infektionFreitext,
    schutzHandschuhe = schutzHandschuhe,
    schutzMundschutz = schutzMundschutz,
    schutzSchutzbrille = schutzSchutzbrille,
    schutzSchutzkittel = schutzSchutzkittel,
    schutzFFP2 = schutzFFP2,
    schutzSonstiges = schutzSonstiges,
    expositionStichverletzung = expositionStichverletzung,
    expositionSchleimhaut = expositionSchleimhaut,
    expositionHautkontakt = expositionHautkontakt,
    expositionKeine = expositionKeine,
    fahrzeugDesinfiziert = fahrzeugDesinfiziert,
    geraeteDesinfiziert = geraeteDesinfiziert,
    waescheGewechselt = waescheGewechselt,
    desinfektionsmittel = desinfektionsmittel,
    desinfektionDurchgefuehrtVon = desinfektionDurchgefuehrtVon,
    bemerkungen = bemerkungen
)

// ---- TransportRefusal ----

fun TransportRefusalEntity.toDomain(): TransportRefusal = TransportRefusal(
    id = id,
    patientId = patientId,
    enabled = enabled,
    patientName = patientName,
    geburtsdatum = geburtsdatum,
    geburtsort = geburtsort,
    datum = datum,
    uhrzeit = if (uhrzeit.isNotBlank()) java.time.LocalTime.parse(uhrzeit) else null,
    lehntBehandlungAb = lehntBehandlungAb,
    lehntTransportAb = lehntTransportAb,
    nichtAuszuschliessendeErkrankungen = nichtAuszuschliessendeErkrankungen,
    moeglicheFolgen = moeglicheFolgen,
    nameZeugeAngehoeriger = nameZeugeAngehoeriger,
    adresseZeugeAngehoeriger = adresseZeugeAngehoeriger,
    nameZeugeRettungsdienst = nameZeugeRettungsdienst,
    nameRettungsdienstNotarzt = nameRettungsdienstNotarzt,
    signaturePatient = signaturePatient,
    ort = ort
)

fun TransportRefusal.toEntity(): TransportRefusalEntity = TransportRefusalEntity(
    id = id,
    patientId = patientId,
    enabled = enabled,
    patientName = patientName,
    geburtsdatum = geburtsdatum,
    geburtsort = geburtsort,
    datum = datum,
    uhrzeit = uhrzeit?.toString() ?: "",
    lehntBehandlungAb = lehntBehandlungAb,
    lehntTransportAb = lehntTransportAb,
    nichtAuszuschliessendeErkrankungen = nichtAuszuschliessendeErkrankungen,
    moeglicheFolgen = moeglicheFolgen,
    nameZeugeAngehoeriger = nameZeugeAngehoeriger,
    adresseZeugeAngehoeriger = adresseZeugeAngehoeriger,
    nameZeugeRettungsdienst = nameZeugeRettungsdienst,
    nameRettungsdienstNotarzt = nameRettungsdienstNotarzt,
    signaturePatient = signaturePatient,
    ort = ort
)
