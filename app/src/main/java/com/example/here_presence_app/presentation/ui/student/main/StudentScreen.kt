package com.example.here_presence_app.presentation.ui.student.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class Course(
    val subject: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val status: Boolean? // true = présent, false = absent, null = futur
)

@Composable
fun StudentScreen() {
    val subjects = listOf("Maths", "Physique", "Biologie")
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val mockCourses = remember { generateMockCourses(subjects) }

    val filtered = remember(selectedSubject, selectedDate) {
        mockCourses.filter {
            (selectedSubject == null || it.subject == selectedSubject) &&
                    (selectedDate == null || it.date == selectedDate)
        }.sortedBy { it.startTime }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Suivi des présences",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profil",
                tint = Color(0xFF1B5E20),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        FilterSection(subjects, selectedSubject, { selectedSubject = it }, selectedDate, { selectedDate = it })

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { course ->
                CourseCard(course)
            }
            if (filtered.isEmpty()) {
                item {
                    Text("Aucun cours trouvé", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    subjects: List<String>,
    selectedSubject: String?,
    onSubjectSelected: (String?) -> Unit,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Text("Choisissez une matière")
    Spacer(Modifier.height(4.dp))
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedSubject ?: "Toutes les matières")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Toutes les matières") }, onClick = {
                onSubjectSelected(null)
                expanded = false
            })
            subjects.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSubjectSelected(it)
                    expanded = false
                })
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    DatePickerSection(selectedDate, onDateSelected)
}

@Composable
fun DatePickerSection(selectedDate: LocalDate?, onDateSelected: (LocalDate?) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Date : ${selectedDate?.format(formatter) ?: "Toutes"}")
        Spacer(Modifier.width(8.dp))
        Button(onClick = { onDateSelected(today) }) {
            Text("Aujourd'hui")
        }
        Spacer(Modifier.width(4.dp))
        Button(onClick = { onDateSelected(null) }) {
            Text("Effacer")
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    val cardColor = when (course.status) {
        true -> Color(0xFFD7ECD9)
        false -> Color(0xFFFCDADA)
        null -> Color(0xFFEEEEEE)
    }
    val statusText = when (course.status) {
        true -> "Présent"
        false -> "Absent"
        null -> "À venir"
    }
    val statusColor = when (course.status) {
        true -> Color(0xFF1B5E20)
        false -> Color(0xFFC62828)
        null -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(course.subject, fontWeight = FontWeight.Bold)
                Text(course.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                Text(course.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
            }
            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}

fun generateMockCourses(subjects: List<String>): List<Course> = buildList {
    val today = LocalDate.now()
    for (subject in subjects) {
        for (i in -3..3) {
            val date = today.plusDays(i.toLong())
            listOf(8, 10, 14).forEach { hour ->
                val time = LocalTime.of(hour, 0)
                val status = if (date.isBefore(today) || (date == today && time.isBefore(LocalTime.now())))
                    Random.nextBoolean()
                else null
                add(Course(subject, date, time, status))
            }
        }
    }
    shuffle()
}