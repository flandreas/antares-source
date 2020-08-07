import React from "react";
import { Stack, Flex, Icon, Text, Checkbox, CheckboxGroup, Box, Select, useColorMode } from "@chakra-ui/core";

const PageLinks = () => (
  <Stack spacing={0} mb={8}>
    <Flex align="center" p={1}>
      <Icon name="phone" mr={3} w="24px" />
      <Text fontWeight="bold">Hello</Text>
    </Flex>
    <Flex align="center" p={1}>
      <Icon name="phone" mr={3} w="24px" />
      <Text fontWeight="bold">Hello</Text>
    </Flex>
    <Flex align="center" p={1}>
      <Icon name="phone" mr={3} w="24px" />
      <Text fontWeight="bold">Hello</Text>
    </Flex>
    <Flex align="center" p={1}>
      <Icon name="phone" mr={3} w="24px" />
      <Text fontWeight="bold">Hello</Text>
    </Flex>
  </Stack>
);

const Filters = () => {
  const { colorMode } = useColorMode();
  const inputBg = { light: "#EDF2F7", dark: "gray.700" };

  return (
    <Stack spacing={8} mb={8}>
      <Box>
        <Text mb={2} fontWeight="bold">
          {"Location"}
        </Text>
        <Select defaultValue="Somewhere" bg={inputBg[colorMode]}>
          <option>Somewhere</option>
        </Select>
      </Box>
      <Box>
        <Text mb={2} fontWeight="bold">
          {"Showing Deals For"}
        </Text>
        <Select defaultValue={"Monday"} bg={inputBg[colorMode]}>
          <option value="Monday">Monday</option>
          <option value="Tuesday">Tuesday</option>
          <option value="Wednesday">Wednesday</option>
          <option value="Thursday">Thursday</option>
          <option value="Friday">Friday</option>
          <option value="Saturday">Saturday</option>
          <option value="Sunday">Sunday</option>
        </Select>
      </Box>

      <Box>
        <Text mb={2} fontWeight="bold">
          Deal Type
        </Text>
        <CheckboxGroup colorScheme="teal" value={["WINE"]}>
          <Stack spacing={2}>
            <Checkbox value="BEER">Beer</Checkbox>
            <Checkbox value="WINE">Wine</Checkbox>
            <Checkbox value="LIQUOR">Liquor</Checkbox>
            <Checkbox value="FOOD">Food</Checkbox>
          </Stack>
        </CheckboxGroup>
      </Box>
    </Stack>
  );
};

const SideNav: React.FC<any> = (props) => {
  const { colorMode } = useColorMode();

  return (
    <Box
      backgroundColor={colorMode === "light" ? "white" : "gray.800"}
      position="fixed"
      left="0"
      width="100%"
      height="100%"
      top="0"
      right="0"
      {...props}
    >
      <Box top="4rem" position="relative" overflowY="auto" borderRightWidth="1px">
        <Box>
          <Flex justify="space-between" direction="column" height="calc(100vh - 4rem)" fontSize="sm" p="6">
            <PageLinks />
            <Filters />
          </Flex>
        </Box>
      </Box>
    </Box>
  );
};

export default SideNav;
