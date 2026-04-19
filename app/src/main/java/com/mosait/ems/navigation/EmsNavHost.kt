package com.mosait.ems.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mosait.ems.feature.overview.OverviewScreen
import com.mosait.ems.feature.mission.MissionDetailScreen
import com.mosait.ems.feature.mission.MissionCreateScreen
import com.mosait.ems.feature.patient.PatientDetailScreen
import com.mosait.ems.feature.patient.PatientCreateScreen
import com.mosait.ems.feature.patient.InitialAssessmentScreen
import com.mosait.ems.feature.patient.NotfallgeschehenScreen
import com.mosait.ems.feature.patient.DiagnosisScreen
import com.mosait.ems.feature.patient.InjuryScreen
import com.mosait.ems.feature.patient.InfectionProtocolScreen
import com.mosait.ems.feature.patient.TransportRefusalScreen
import com.mosait.ems.feature.patient.VitalsScreen
import com.mosait.ems.feature.patient.MeasuresScreen
import com.mosait.ems.feature.patient.ResultScreen
import com.mosait.ems.feature.export.ExportScreen
import com.mosait.ems.settings.SettingsScreen

@Composable
fun EmsNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Overview
    ) {
        composable<Route.Overview> {
            OverviewScreen(
                onCreateMission = {
                    navController.navigate(Route.MissionCreate())
                },
                onMissionClick = { missionId ->
                    navController.navigate(Route.MissionDetail(missionId))
                },
                onSettingsClick = {
                    navController.navigate(Route.Settings)
                }
            )
        }

        composable<Route.MissionCreate> {
            MissionCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onMissionCreated = { missionId ->
                    navController.navigate(Route.MissionDetail(missionId)) {
                        popUpTo(Route.Overview) { inclusive = false }
                    }
                }
            )
        }

        composable<Route.MissionDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MissionDetail>()
            MissionDetailScreen(
                missionId = route.missionId,
                onNavigateBack = { navController.popBackStack() },
                onAddPatient = { missionId ->
                    navController.navigate(Route.PatientCreate(missionId))
                },
                onPatientClick = { missionId, patientId ->
                    navController.navigate(Route.PatientDetail(missionId, patientId))
                },
                onExport = { missionId ->
                    navController.navigate(Route.Export(missionId))
                },
                onEditMission = { missionId ->
                    navController.navigate(Route.MissionEdit(missionId))
                },
                onEditPatient = { missionId, patientId ->
                    navController.navigate(Route.PatientEdit(missionId, patientId))
                }
            )
        }

        composable<Route.PatientCreate> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PatientCreate>()
            PatientCreateScreen(
                missionId = route.missionId,
                onNavigateBack = { navController.popBackStack() },
                onPatientCreated = { missionId, patientId ->
                    navController.navigate(Route.PatientDetail(missionId, patientId)) {
                        popUpTo(Route.MissionDetail(missionId)) { inclusive = false }
                    }
                }
            )
        }

        composable<Route.MissionEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.MissionEdit>()
            MissionCreateScreen(
                onNavigateBack = { navController.popBackStack() },
                onMissionCreated = { navController.popBackStack() }
            )
        }

        composable<Route.PatientEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PatientEdit>()
            PatientCreateScreen(
                missionId = route.missionId,
                onNavigateBack = { navController.popBackStack() },
                onPatientCreated = { _, _ -> navController.popBackStack() }
            )
        }

        composable<Route.PatientDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.PatientDetail>()
            PatientDetailScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() },
                onEditPatient = { patientId ->
                    navController.navigate(Route.PatientEdit(route.missionId, patientId))
                },
                onNavigateToAssessment = { patientId ->
                    navController.navigate(Route.InitialAssessment(patientId))
                },
                onNavigateToNotfallgeschehen = { patientId ->
                    navController.navigate(Route.Notfallgeschehen(patientId))
                },
                onNavigateToDiagnosis = { patientId ->
                    navController.navigate(Route.Diagnosis(patientId))
                },
                onNavigateToInjury = { patientId ->
                    navController.navigate(Route.Injury(patientId))
                },
                onNavigateToVitals = { patientId ->
                    navController.navigate(Route.Vitals(patientId))
                },
                onNavigateToMeasures = { patientId ->
                    navController.navigate(Route.Measures(patientId))
                },
                onNavigateToResult = { patientId ->
                    navController.navigate(Route.Result(patientId))
                },
                onNavigateToInfectionProtocol = { patientId ->
                    navController.navigate(Route.InfectionProtocol(patientId))
                },
                onNavigateToTransportRefusal = { patientId ->
                    navController.navigate(Route.TransportRefusal(patientId))
                }
            )
        }

        composable<Route.InitialAssessment> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.InitialAssessment>()
            InitialAssessmentScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Notfallgeschehen> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Notfallgeschehen>()
            NotfallgeschehenScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Diagnosis> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Diagnosis>()
            DiagnosisScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Injury> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Injury>()
            InjuryScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Vitals> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Vitals>()
            VitalsScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Measures> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Measures>()
            MeasuresScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Result> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Result>()
            ResultScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.InfectionProtocol> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.InfectionProtocol>()
            InfectionProtocolScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.TransportRefusal> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.TransportRefusal>()
            TransportRefusalScreen(
                patientId = route.patientId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Export> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Export>()
            ExportScreen(
                missionId = route.missionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
