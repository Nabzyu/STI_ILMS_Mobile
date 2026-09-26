package com.example.stismartlib.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.stismartlib.R

@Composable
fun AppLogo(
    modifier: Modifier = Modifier
) {

    Image(
        painter = painterResource(id = R.drawable.sti_logo),
        contentDescription = "STISmartLib Logo",
        modifier = modifier.size(150.dp)
    )

}