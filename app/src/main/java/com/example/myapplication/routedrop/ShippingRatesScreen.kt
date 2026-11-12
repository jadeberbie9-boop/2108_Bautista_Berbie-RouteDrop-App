package com.example.myapplication.routedrop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingRatesScreen(paddingValues: PaddingValues) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var weight by remember { mutableStateOf("1") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color(0xFFF8F9FA))
    ) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color.Red,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Domestic shipment", color = if (selectedTab == 0) Color.Red else Color.Gray, fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("International shipment", color = if (selectedTab == 1) Color.Red else Color.Gray) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Origin and Destination Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // From
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.Red, shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("From", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Origin:City", color = Color.Gray, fontSize = 14.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.padding(start = 40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // To
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFFFB300), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("To", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Destination:City", color = Color.Gray, fontSize = 14.sp)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
                    }
                }
            }

            // Item details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Item", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Service Type
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Service type", color = Color.Gray, fontSize = 12.sp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Transparent)
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("J&T Super", fontSize = 16.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                            HorizontalDivider()
                        }
                        
                        // Weight
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weight", color = Color.Gray, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
                                )
                                Text("KG", fontSize = 16.sp)
                            }
                            HorizontalDivider()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Packaging Service
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF5F5), RoundedCornerShape(8.dp))
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF5F5), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(12.dp)) {
                                Text("Packaging service", fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)).padding(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Goods Category", color = Color.Gray, fontSize = 12.sp)
                                    Text("PARCEL", fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))
                                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Package Type", color = Color.Gray, fontSize = 16.sp)
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Dimension
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Dimension", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Dimensional Weight: ", fontSize = 14.sp)
                        Text("0 KG", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Length
                        OutlinedTextField(
                            value = length,
                            onValueChange = { length = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Length") },
                            trailingIcon = { Text("cm", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        Text(" * ", modifier = Modifier.padding(horizontal = 4.dp), color = Color.Gray)
                        // Width
                        OutlinedTextField(
                            value = width,
                            onValueChange = { width = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Width") },
                            trailingIcon = { Text("cm", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        Text(" * ", modifier = Modifier.padding(horizontal = 4.dp), color = Color.Gray)
                        // Height
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Height") },
                            trailingIcon = { Text("cm", color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.LightGray
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Length(cm), Width(cm), Height(cm) must less than 180cm", color = Color.Gray, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Check", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
