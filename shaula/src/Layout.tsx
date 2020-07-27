import React from "react";
import {
  Box,
  Flex,
  IconButton,
  useColorMode,
  InputGroup,
  InputLeftElement,
  Input,
  Text,
  Stack,
  Icon,
  Select,
  Checkbox,
  CheckboxGroup,
} from "@chakra-ui/core";

const Header: React.FC = () => {
  const bg = { light: "white", dark: "gray.800" };
  const { colorMode, toggleColorMode } = useColorMode();

  return (
    <Box
      pos="fixed"
      as="header"
      top="0"
      zIndex={4}
      bg={bg[colorMode]}
      left="0"
      right="0"
      borderBottomWidth="1px"
      width="full"
      height="4rem"
    >
      <Box width="full" mx="auto" px={6} pr={[1, 6]} height="100%">
        <Flex size="100%" p={[0, 6]} pl={[0, 4]} align="center" justify="space-between">
          <Box as="a" d="block" aria-label="daydrink, Back to homepage">
            <Icon name="attachment" size="20px" />
          </Box>
          <InputGroup width="100%" ml={16} mr={16}>
            <InputLeftElement>
              <Icon name="search" color="gray.500" />
            </InputLeftElement>
            <Input
              type="text"
              placeholder={`Search for deals (Press "/" to focus)`}
              bg={colorMode === "light" ? "gray.100" : "gray.700"}
            />
          </InputGroup>

          <Flex align="center" color="gray.500">
            <IconButton
              aria-label={`Switch to ${colorMode === "light" ? "dark" : "light"} mode`}
              variant="ghost"
              color="current"
              ml="2"
              fontSize="20px"
              icon={colorMode === "light" ? "moon" : "sun"}
              onClick={toggleColorMode}
            />
          </Flex>
        </Flex>
      </Box>
    </Box>
  );
};

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
          {"Deal Type"}
        </Text>
        <CheckboxGroup spacing={2} variantColor="teal" value={["WINE"]}>
          <Checkbox value="BEER">Beer</Checkbox>
          <Checkbox value="WINE">Wine</Checkbox>
          <Checkbox value="LIQUOR">Liquor</Checkbox>
          <Checkbox value="FOOD">Food</Checkbox>
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

const Layout: React.FC = () => {
  const { colorMode } = useColorMode();

  return (
    <>
      <Header />
      <Box>
        <SideNav display={["none", null, "block"]} maxWidth="18rem" width="full" />
        <Box pl={[0, null, "18rem"]} mt="4rem">
          <Box
            as="section"
            backgroundColor={colorMode === "light" ? "gray.100" : "gray.900"}
            minHeight="calc(100vh - 4rem)"
          >
            <Box>
              <p>This is content</p>
            </Box>
          </Box>
        </Box>
      </Box>
    </>
  );
};

export default Layout;
