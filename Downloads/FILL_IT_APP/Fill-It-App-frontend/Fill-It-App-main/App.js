import 'react-native-gesture-handler';
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Image } from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { TripRequestProvider } from './screens/Driver/TripRequestStore';


import LoginScreen from './screens/auth/LoginScreen';
import SignupScreen from './screens/auth/SignupScreen';
import RoleSelectionScreen from './screens/RoleSelectionScreen';

import HomeScreen from './screens/Customer/HomeScreen';
import HistoryScreen from './screens/Customer/HistoryScreen';
import AccountScreen from './screens/Customer/AccountScreen';

import DriverHomeScreen from './screens/Driver/DriverHomeScreen';
import DriverMyTripsScreen from './screens/Driver/DriverMyTripsScreen';
import DriverAccountScreen from './screens/Driver/DriverAccountScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const COLORS = {
  accent: '#FF7518',
  textPrimary: '#ECF0F1',
  bottomNavBackground: '#081219',
};

const commonTabOptions = {
  headerShown: false,
  tabBarStyle: {
    backgroundColor: COLORS.bottomNavBackground,
    borderTopWidth: 0,
    height: 100,
    paddingTop: 15,
  },
  tabBarLabelStyle: {
    fontSize: 14,
    fontWeight: '600',
    paddingBottom: 10,
  },
  tabBarActiveTintColor: COLORS.accent,
  tabBarInactiveTintColor: COLORS.textPrimary,
};

function CustomerTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        ...commonTabOptions,
        tabBarIcon: ({ focused, color }) => {
          let iconName;

          if (route.name === 'Home') {
            iconName = 'home';
          } else if (route.name === 'History') {
            iconName = 'history';
          } else if (route.name === 'Account') {
            iconName = 'person';
          }
          return <Icon name={iconName} size={32} color={color} />;
        },
      })}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          tabBarLabel: 'Home',
        }}
      />
      <Tab.Screen
        name="History"
        component={HistoryScreen}
        options={{
          tabBarLabel: 'History',
        }}
      />
      <Tab.Screen
        name="Account"
        component={AccountScreen}
        options={{
          tabBarLabel: 'Account',
        }}
      />
    </Tab.Navigator>
  );
}

function DriverTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        ...commonTabOptions,
        tabBarIcon: ({ focused, color }) => {
          let iconName;
          if (route.name === 'DriverHome') {
            iconName = 'home';
          } else if (route.name === 'DriverMyTrips') {
            iconName = 'commute';
          } else if (route.name === 'DriverAccount') {
            iconName = 'person';
          }
          return <Icon name={iconName} size={32} color={color} />;
        },
      })}
    >
      <Tab.Screen
        name="DriverHome"
        component={DriverHomeScreen}
        options={{
          tabBarLabel: 'Home',
        }}
      />
      <Tab.Screen
        name="DriverMyTrips"
        component={DriverMyTripsScreen}
        options={{
          tabBarLabel: 'My Trips',
        }}
      />
      <Tab.Screen
        name="DriverAccount"
        component={DriverAccountScreen}
        options={{
          tabBarLabel: 'Account',
        }}
      />
    </Tab.Navigator>
  );
}

export default function App() {
  return (
    <TripRequestProvider>
      <NavigationContainer>
        <Stack.Navigator initialRouteName="Login">
          <Stack.Screen
            name="Login"
            component={LoginScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="Signup"
            component={SignupScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="RoleSelection"
            component={RoleSelectionScreen}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="CustomerFlow"
            component={CustomerTabs}
            options={{ headerShown: false }}
          />
          <Stack.Screen
            name="DriverFlow"
            component={DriverTabs}
            options={{ headerShown: false }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </TripRequestProvider>
  );
}
